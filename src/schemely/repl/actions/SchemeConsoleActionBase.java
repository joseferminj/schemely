package schemely.repl.actions;

import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.content.Content;
import schemely.psi.impl.SchemeFile;
import schemely.repl.SchemeConsole;
import schemely.repl.toolwindow.REPLToolWindowFactory;
import schemely.scheme.Scheme;

public abstract class SchemeConsoleActionBase extends AnAction
{
  private static final Logger LOG = Logger.getInstance(SchemeConsoleActionBase.class.getName());

  protected static void executeCommand(Project project, String command)
  {
    Scheme.REPL activeRepl = findActiveRepl(project);
    LOG.assertTrue(activeRepl != null);

    SchemeConsole languageConsole = activeRepl.getConsoleView().getConsole();
    languageConsole.printToHistory(languageConsole.getPrompt(), ConsoleViewContentType.USER_INPUT.getAttributes());
    languageConsole.printToHistory(command + "\n", ConsoleViewContentType.NORMAL_OUTPUT.getAttributes());

    if (!StringUtil.isEmptyOrSpaces(command))
    {
      languageConsole.getHistoryModel().addToHistory(command);
    }

    activeRepl.execute(command);
  }

  @Override
  public void update(AnActionEvent e)
  {
    Presentation presentation = e.getPresentation();

    Editor editor = e.getData(PlatformDataKeys.EDITOR);

    if (editor == null)
    {
      presentation.setEnabled(false);
      return;
    }
    Project project = editor.getProject();
    if (project == null)
    {
      presentation.setEnabled(false);
      return;
    }

    Document document = editor.getDocument();
    PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
    if ((psiFile == null) || (!(psiFile instanceof SchemeFile)))
    {
      presentation.setEnabled(false);
      return;
    }

    VirtualFile virtualFile = psiFile.getVirtualFile();
    if ((virtualFile == null) || ((virtualFile instanceof LightVirtualFile)))
    {
      presentation.setEnabled(false);
      return;
    }
    String filePath = virtualFile.getPath();
    if (filePath == null)
    {
      presentation.setEnabled(false);
      return;
    }

    Scheme.REPL activeRepl = findActiveRepl(project);
    if (activeRepl == null)
    {
      presentation.setEnabled(false);
      return;
    }

    LanguageConsoleImpl console = activeRepl.getConsoleView().getConsole();
    if (!(console instanceof SchemeConsole))
    {
      presentation.setEnabled(false);
      return;
    }

    presentation.setEnabled(true);
  }

  protected static Scheme.REPL findActiveRepl(Project project)
  {
    ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
    ToolWindow toolWindow = toolWindowManager.getToolWindow(REPLToolWindowFactory.TOOL_WINDOW_ID);
    Content content = toolWindow.getContentManager().getSelectedContent();

    if (content != null)
    {
      return content.getUserData(NewSchemeConsoleAction.REPL_KEY);
    }

    return null;
  }
}
