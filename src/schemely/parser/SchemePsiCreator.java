package schemely.parser;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import schemely.psi.impl.SchemeQuoted;
import schemely.psi.impl.SchemeLiteral;
import schemely.psi.impl.SchemeVector;
import schemely.psi.impl.list.SchemeList;
import schemely.psi.impl.symbols.SchemeIdentifier;


public class SchemePsiCreator
{
  public static PsiElement createElement(ASTNode node)
  {
    IElementType elementType = node.getElementType();

    if (elementType == AST.LIST)
    {
      return new SchemeList(node);
    }
    if (elementType == AST.VECTOR)
    {
      return new SchemeVector(node);
    }
    if (elementType == AST.QUOTED)
    {
      return new SchemeQuoted(node);
    }
    if (elementType == AST.IDENTIFIER)
    {
      return new SchemeIdentifier(node);
    }
    if (elementType == AST.SPECIAL)
    {
      return new SchemeIdentifier(node);
    }
    if (elementType == AST.LITERAL)
    {
      return new SchemeLiteral(node);
    }

    throw new Error("Unexpected ASTNode: " + node.getElementType());
  }
}
