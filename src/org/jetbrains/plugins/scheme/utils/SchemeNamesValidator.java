package org.jetbrains.plugins.scheme.utils;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.scheme.lexer.Tokens;
import org.jetbrains.plugins.scheme.parser.SchemeElementType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Colin Fleming
 */
public class SchemeNamesValidator implements NamesValidator
{
  private static final Set<String> keywords = new HashSet<String>();
  private static final Set<String> specialIdentifiers = new HashSet<String>();

  static
  {
    for (IElementType type : Tokens.SYNTACTIC_KEYWORDS.getTypes())
    {
      keywords.add(((SchemeElementType) type).getName());
    }

    specialIdentifiers.addAll(Arrays.asList("+", "-", "..."));
  }

  public boolean isKeyword(String s, Project project)
  {
    return keywords.contains(s);
  }

  public boolean isIdentifier(String s, Project project)
  {
    if (specialIdentifiers.contains(s))
    {
      return true;
    }

    if (!isInitial(s.charAt(0)))
    {
      return false;
    }

    for (int i = 1; i < s.length(); i++)
    {
      if (!isSubsequent(s.charAt(i)))
      {
        return false;
      }
    }

    return true;
  }

  private boolean isInitial(char c)
  {
    return Character.isLetter(c) || ("!$%&*/:<=>?^_~".indexOf(c) >= 0);
  }

  private boolean isSubsequent(char c)
  {
    return isInitial(c) || Character.isDigit(c) || ("+-.@".indexOf(c) >= 0);
  }
}