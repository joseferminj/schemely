package org.jetbrains.plugins.scheme.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.plugins.scheme.psi.impl.SchemePsiElementBase;
import org.jetbrains.plugins.scheme.psi.impl.list.SchemeList;
import org.jetbrains.plugins.scheme.psi.impl.symbols.SchemeIdentifier;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class SchemeStructureViewElement implements StructureViewTreeElement
{
  private PsiElement myElement;

  public SchemeStructureViewElement(PsiElement element)
  {
    myElement = element;
  }

  public PsiElement getValue()
  {
    return myElement;
  }

  public void navigate(boolean requestFocus)
  {
    ((NavigationItem) myElement).navigate(requestFocus);
  }

  public boolean canNavigate()
  {
    return ((NavigationItem) myElement).canNavigate();
  }

  public boolean canNavigateToSource()
  {
    return ((NavigationItem) myElement).canNavigateToSource();
  }

  public StructureViewTreeElement[] getChildren()
  {
    final List<SchemePsiElementBase> childrenElements = new ArrayList<SchemePsiElementBase>();
    myElement.acceptChildren(new PsiElementVisitor()
    {
      public void visitElement(PsiElement element)
      {
        if (isBrowsableElement(element))
        {
          childrenElements.add((SchemePsiElementBase) element);
        }
        else
        {
          element.acceptChildren(this);
        }
      }
    });

    StructureViewTreeElement[] children = new StructureViewTreeElement[childrenElements.size()];
    for (int i = 0; i < children.length; i++)
    {
      children[i] = new SchemeStructureViewElement(childrenElements.get(i));
    }

    return children;
  }

  private boolean isBrowsableElement(PsiElement element)
  {
    if (element instanceof SchemeIdentifier)
    {
      SchemeIdentifier identifier = (SchemeIdentifier) element;

      PsiElement parentElement = identifier.getParent();
      if (!(parentElement instanceof SchemeList))
      {
        return false;
      }

      SchemeList parent = (SchemeList) parentElement;
      if (parent.isDefinition() && (parent.getSecondNonLeafElement() == element))
      {
        // (define x <whatever>)
        return true;
      }
      else if (parent.getParent() instanceof SchemeList)
      {
        SchemeList grandparent = (SchemeList) parent.getParent();
        if (grandparent.isDefinition() &&
            (grandparent.getSecondNonLeafElement() == parent) &&
            (parent.getFirstIdentifier() == element))
        {
          // (define (x <formals>) <whatever>)
          return true;
        }
      }
    }
    return false;
  }

  public ItemPresentation getPresentation()
  {
    return new ItemPresentation()
    {
      public String getPresentableText()
      {
        return ((PsiNamedElement) myElement).getName();
      }

      public TextAttributesKey getTextAttributesKey()
      {
        return null;
      }

      public String getLocationString()
      {
        return null;
      }

      public Icon getIcon(boolean open)
      {
        return myElement.getIcon(Iconable.ICON_FLAG_OPEN);
      }
    };
  }
}