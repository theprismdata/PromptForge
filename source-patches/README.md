# Source Patches

This directory contains source-level patches corresponding to the release bundle.

## assistai/

Patched AssistAI plugin sources:
- `plugin.xml`: command/menu additions (`AI Settings`)
- `models-defaults.xml`: default model list (OpenAI/Claude/vLLM)
- `EclipseIntegrationsMcpServer.java`: `createProject` tool exposure
- `ProjectService.java`: robust project creation + scaffold fallback
- `CodeEditingService.java`: auto-create project on file/directory operations
- `EclipseRunnerMcpServer.java`: `runShellCommand` tool
- `OpenAssistAISettingsHandler.java`: opens AssistAI preferences from main menu

## eclipse-ui-ide/

Patched Eclipse UI IDE bundle sources:
- `plugin.xml`
- `plugin.properties`
- `CreateJava21GradleProjectHandler.java`

These enable prompt-driven project creation commands used by the agent.
