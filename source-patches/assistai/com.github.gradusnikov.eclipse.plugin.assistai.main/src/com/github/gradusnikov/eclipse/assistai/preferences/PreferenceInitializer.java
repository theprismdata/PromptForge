package com.github.gradusnikov.eclipse.assistai.preferences;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.gradusnikov.eclipse.assistai.Activator;
import com.github.gradusnikov.eclipse.assistai.mcp.McpServerRepository;
import com.github.gradusnikov.eclipse.assistai.models.ModelApiDescriptor;
import com.github.gradusnikov.eclipse.assistai.models.ModelApiDescriptorRepository;
import com.github.gradusnikov.eclipse.assistai.preferences.mcp.McpServerDescriptorUtilities;
import com.github.gradusnikov.eclipse.assistai.prompt.PromptLoader;
import com.github.gradusnikov.eclipse.assistai.prompt.Prompts;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Class used to initialize default preference values.
 */
@Creatable
@Singleton
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
    private static final String DEFAULT_MODELS_XML_PATH = "models-defaults.xml";

    @Inject
    private ModelApiDescriptorRepository modelApiDescriptorRepository;

    private McpServerRepository mcpServerRepository;

    private static final class DefaultModelsConfig
    {
        private final List<ModelApiDescriptor> models;
        private final String defaultChatModelId;

        private DefaultModelsConfig(List<ModelApiDescriptor> models, String defaultChatModelId)
        {
            this.models = models;
            this.defaultChatModelId = defaultChatModelId;
        }
    }

    public void init()
    {
        modelApiDescriptorRepository = Activator.getDefault().getModelApiDescriptorRepository();
        mcpServerRepository = Activator.getDefault().make( McpServerRepository.class );
    }

    public void initializeDefaultPreferences()
    {
        init();

        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        DefaultModelsConfig defaultModels = loadDefaultModelsFromXml().orElseGet(this::fallbackDefaultModels);
        modelApiDescriptorRepository.initializeDefaultDescriptors(
                defaultModels.models.toArray(new ModelApiDescriptor[0]));
        modelApiDescriptorRepository.initializeDefaultDescriptorInUse(resolveDefaultChatModel(defaultModels));
        // Also set runtime values to recover from previously persisted empty model lists.
        store.setValue(PreferenceConstants.ASSISTAI_DEFINED_MODELS,
                ModelApiDescriptorRepository.toJson(defaultModels.models));
        store.setValue(PreferenceConstants.ASSISTAI_CHAT_MODEL,
                resolveDefaultChatModel(defaultModels).uid());

        var descriptors = mcpServerRepository.listBuiltInServers();

        String mcpServersJson = McpServerDescriptorUtilities.toJson( descriptors );
        store.setDefault(PreferenceConstants.ASSISTAI_DEFINED_MCP_SERVERS, mcpServersJson);

        // Initialize HTTP MCP Server defaults
        store.setDefault(PreferenceConstants.ASSISTAI_MCP_HTTP_HOSTNAME, "localhost");
        store.setDefault(PreferenceConstants.ASSISTAI_MCP_HTTP_PORT, 8080);
        store.setDefault(PreferenceConstants.ASSISTAI_MCP_HTTP_AUTH_TOKEN, UUID.randomUUID().toString());
        store.setDefault(PreferenceConstants.ASSISTAI_MCP_HTTP_ENABLED, false);

        // Initialize Code Completion defaults
        store.setDefault(PreferenceConstants.ASSISTAI_COMPLETION_ENABLED, true);
        store.setDefault(PreferenceConstants.ASSISTAI_COMPLETION_MODEL, ""); // Empty means use chat model
        store.setDefault(PreferenceConstants.ASSISTAI_COMPLETION_TIMEOUT_SECONDS, 8);
        store.setDefault(PreferenceConstants.ASSISTAI_COMPLETION_HOTKEY, PreferenceConstants.ASSISTAI_COMPLETION_HOTKEY_DEFAULT);

        PromptLoader promptLoader = new PromptLoader();
        for ( Prompts prompt : Prompts.values() )
        {
            store.setDefault( prompt.preferenceName(), promptLoader.getDefaultPrompt( prompt.getFileName() ) );
        }
    }

    private Optional<DefaultModelsConfig> loadDefaultModelsFromXml()
    {
        try
        {
            URL entry = Activator.getDefault().getBundle().getEntry(DEFAULT_MODELS_XML_PATH);
            if (entry == null)
            {
                return Optional.empty();
            }

            try (InputStream in = entry.openStream())
            {
                var builderFactory = DocumentBuilderFactory.newInstance();
                var builder = builderFactory.newDocumentBuilder();
                var document = builder.parse(in);
                var root = document.getDocumentElement();
                if (root == null || !"models".equals(root.getTagName()))
                {
                    return Optional.empty();
                }

                String defaultChatModelId = root.getAttribute("defaultChatModelId");
                NodeList nodes = root.getElementsByTagName("model");
                List<ModelApiDescriptor> models = new ArrayList<>();

                for (int i = 0; i < nodes.getLength(); i++)
                {
                    Element e = (Element) nodes.item(i);
                    models.add(new ModelApiDescriptor(
                            attr(e, "uid", ""),
                            attr(e, "apiType", ""),
                            attr(e, "apiUrl", ""),
                            attr(e, "apiKey", ""),
                            parseInt(attr(e, "connectionTimeoutSeconds", "10"), 10),
                            parseInt(attr(e, "requestTimeoutSeconds", "30"), 30),
                            attr(e, "modelName", ""),
                            parseInt(attr(e, "temperature", "7"), 7),
                            parseBoolean(attr(e, "vision", "true"), true),
                            parseBoolean(attr(e, "functionCalling", "true"), true)));
                }

                if (models.isEmpty())
                {
                    return Optional.empty();
                }
                return Optional.of(new DefaultModelsConfig(models, defaultChatModelId));
            }
        }
        catch (Exception ignored)
        {
            return Optional.empty();
        }
    }

    private DefaultModelsConfig fallbackDefaultModels()
    {
        List<ModelApiDescriptor> models = new ArrayList<>();
        models.add(new ModelApiDescriptor("5e8d3a9f-c5e2-4c1d-9f3b-a7e6b4d2c1e0", "openai",
                "https://api.openai.com/v1/chat/completions", "", 10, 30, "gpt-4o", 7, true, true));
        models.add(new ModelApiDescriptor("8d099c40-5a01-483b-878f-bfed8c0d1bbe", "claude",
                "https://api.anthropic.com/v1/messages", "", 10, 30, "claude-sonnet-4-6", 7, true, true));
        models.add(new ModelApiDescriptor("d2a0d8e1-96f8-4f7c-8ec4-8d8b6677b109", "vllm",
                "http://localhost:8000/v1/chat/completions", "", 10, 30, "Qwen/Qwen2.5-Coder-32B-Instruct", 7, false, true));
        return new DefaultModelsConfig(models, "5e8d3a9f-c5e2-4c1d-9f3b-a7e6b4d2c1e0");
    }

    private ModelApiDescriptor resolveDefaultChatModel(DefaultModelsConfig config)
    {
        if (config.defaultChatModelId != null && !config.defaultChatModelId.isBlank())
        {
            for (ModelApiDescriptor model : config.models)
            {
                if (config.defaultChatModelId.equals(model.uid()))
                {
                    return model;
                }
            }
        }
        return config.models.getFirst();
    }

    private static String attr(Element e, String key, String defaultValue)
    {
        String value = e.getAttribute(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static int parseInt(String value, int defaultValue)
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    private static boolean parseBoolean(String value, boolean defaultValue)
    {
        if (value == null || value.isBlank())
        {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}
