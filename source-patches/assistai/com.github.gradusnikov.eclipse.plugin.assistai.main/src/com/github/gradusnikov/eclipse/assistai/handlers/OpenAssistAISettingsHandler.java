package com.github.gradusnikov.eclipse.assistai.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class OpenAssistAISettingsHandler extends AbstractHandler
{
    private static final String ASSISTAI_ROOT_PAGE_ID = "com.github.gradusnikov.eclipse.assistai.preferences.OpenAIPreferencePage";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        Display display = Display.getDefault();
        if (display == null || display.isDisposed())
        {
            return null;
        }

        display.asyncExec(() -> {
            PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                    null,
                    ASSISTAI_ROOT_PAGE_ID,
                    null,
                    null);
            if (dialog != null)
            {
                dialog.open();
            }
        });
        return null;
    }
}
