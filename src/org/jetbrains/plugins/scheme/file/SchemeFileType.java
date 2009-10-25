package org.jetbrains.plugins.scheme.file;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scheme.SchemeIcons;
import org.jetbrains.plugins.scheme.SchemeLanguage;

import javax.swing.*;

public class SchemeFileType extends LanguageFileType
{
  public static final SchemeFileType SCHEME_FILE_TYPE = new SchemeFileType();
  public static final Language SCHEME_LANGUAGE = SCHEME_FILE_TYPE.getLanguage();
  @NonNls
  public static final String SCHEME_EXTENSIONS = "scm;ss";


  public SchemeFileType()
  {
    super(new SchemeLanguage());
  }

  @NotNull
  public String getName()
  {
    return "Scheme";
  }

  @NotNull
  public String getDescription()
  {
    return "Scheme file";
  }

  @NotNull
  public String getDefaultExtension()
  {
    return "scm";
  }

  public Icon getIcon()
  {
    return SchemeIcons.SCHEME_ICON_16x16;
  }

  public boolean isJVMDebuggingSupported()
  {
    return true;
  }


}