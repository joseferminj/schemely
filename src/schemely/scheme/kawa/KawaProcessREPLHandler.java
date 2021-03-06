package schemely.scheme.kawa;

import com.intellij.execution.CantRunException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.NotNullFunction;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import schemely.scheme.common.REPLUtil;
import schemely.scheme.kawa.impl.KawaREPLMain;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Colin Fleming
 */
public class KawaProcessREPLHandler // implements ProcessREPLHandler
{
  @NonNls
  @Language("RegExp")
  public static final String KAWA_PROMPT = "#\\|[^\\|]*\\|#\\s*";
  public static final Pattern KAWA_PROMPT_PATTERN = Pattern.compile(KAWA_PROMPT);

//  @Override
  public List<String> createRuntimeArgs(Module module, String workingDir) throws CantRunException
  {
    JavaParameters params = new JavaParameters();
    params.configureByModule(module, JavaParameters.JDK_AND_CLASSES);

    params.getClassPath().add(KawaConfigUtil.KAWA_SDK);

    REPLUtil.addSourcesToClasspath(module, params);

    params.setMainClass(KawaREPLMain.class.getName());
    params.setWorkingDirectory(new File(workingDir));

    return REPLUtil.getCommandLine(params);
  }

//  @Override
  public void processOutput(LanguageConsoleImpl console, String text, Key attributes)
  {
    ConsoleViewContentType outputType = ConsoleViewContentType.NORMAL_OUTPUT;
    LanguageConsoleImpl.printToConsole(console, processPrompts(text), outputType, null);
  }

  private static String processPrompts(String text)
  {
    if (text != null)
    {
      String trimmed = text;
      Matcher matcher = KawaProcessREPLHandler.KAWA_PROMPT_PATTERN.matcher(trimmed);
      while (matcher.lookingAt())
      {
        String prefix = matcher.group();
        trimmed = StringUtil.trimStart(trimmed, prefix);
        matcher.reset(trimmed);
      }
      return trimmed;
    }
    return text;
  }

//  @Override
  public NotNullFunction<String, Boolean> getConsoleMatcher()
  {
    return new ConsoleMatcher();
  }

  private static class ConsoleMatcher implements NotNullFunction<String, Boolean>
  {
    @Override
    @NotNull
    public Boolean fun(String cmdLine)
    {
      return Boolean.valueOf((cmdLine != null) && (cmdLine.contains(KawaREPLMain.class.getSimpleName())));
    }
  }
}
