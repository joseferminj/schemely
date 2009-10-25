package org.jetbrains.plugins.scheme;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.scheme.parser.AST;
import static org.jetbrains.plugins.scheme.parser.AST.LIST;
import org.jetbrains.plugins.scheme.psi.impl.SchemeFile;
import org.jetbrains.plugins.scheme.psi.impl.list.SchemeList;

import java.util.ArrayList;
import java.util.List;

public class SchemeFoldingBuilder implements FoldingBuilder
{
  public String getPlaceholderText(ASTNode node)
  {
    IElementType type = node.getElementType();
    PsiElement psi = node.getPsi();
    if (psi instanceof SchemeList)
    {
      String text = ((SchemeList) psi).getPresentableText();
      return "(" + (text != null ? text + " " : "") + "...)";
    }
    throw new Error("Unexpected node: " + type + "-->" + node.getText());
  }

  public boolean isCollapsedByDefault(ASTNode node)
  {
    return false;
  }

  public FoldingDescriptor[] buildFoldRegions(ASTNode node, Document document)
  {
    touchTree(node);
    List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
    appendDescriptors(node, descriptors);
    return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
  }

  /**
   * We have to touch the PSI tree to get the folding to show up when we first open a file
   *
   * @param node given node
   */
  private void touchTree(ASTNode node)
  {
    if (node.getElementType() == AST.FILE)
    {
      node.getPsi().getFirstChild();
    }
  }

  private void appendDescriptors(ASTNode node, List<FoldingDescriptor> descriptors)
  {
    if (isFoldableNode(node))
    {
      descriptors.add(new FoldingDescriptor(node, node.getTextRange()));
    }

    ASTNode child = node.getFirstChildNode();
    while (child != null)
    {
      appendDescriptors(child, descriptors);
      child = child.getTreeNext();
    }
  }

  private boolean isFoldableNode(ASTNode node)
  {
    PsiElement element = node.getPsi();
    IElementType type = node.getElementType();
    if (type == LIST &&
        element.getParent() instanceof SchemeFile &&
        node.getText().contains("\n") &&
        element instanceof SchemeList)
    {
      return true;
    }

    return false; // (type == DEF || type == DEFMETHOD) && node.getText().contains("\n");
  }
}