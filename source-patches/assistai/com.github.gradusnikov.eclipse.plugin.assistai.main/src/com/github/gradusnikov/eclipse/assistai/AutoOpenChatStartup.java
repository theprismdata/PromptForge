package com.github.gradusnikov.eclipse.assistai;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Opens AI Chat view once the workbench starts so users can chat immediately.
 */
public class AutoOpenChatStartup implements IStartup
{
    private static final String CHAT_VIEW_ID = "com.github.gradusnikov.eclipse.assistai.view.ChatView";

    @Override
    public void earlyStartup()
    {
        Display.getDefault().asyncExec(() -> {
            try
            {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window == null)
                {
                    return;
                }
                IWorkbenchPage page = window.getActivePage();
                if (page == null)
                {
                    return;
                }
                page.showView(CHAT_VIEW_ID);
            }
            catch (PartInitException | IllegalStateException ignored)
            {
                // Startup convenience only; do not block workbench startup.
            }
        });
    }
}
