package org.jetbrains.plugins.scheme.psi.impl.symbols;

import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.MethodSignature;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;
import org.jetbrains.plugins.scheme.SchemeIcons;
import org.jetbrains.plugins.scheme.psi.resolve.ResolveUtil;
import org.jetbrains.plugins.scheme.psi.resolve.SchemeResolveResult;
import org.jetbrains.plugins.scheme.psi.resolve.completion.CompletionProcessor;

import java.util.*;


public class CompleteSymbol
{
  public static Object[] getVariants(SchemeIdentifier symbol)
  {
    Collection<Object> variants = new ArrayList<Object>();

    CompletionProcessor processor = new CompletionProcessor(symbol);
//    ResolveUtil.treeWalkUp(symbol, processor);

    SchemeResolveResult[] candidates = processor.getCandidates();
    if (candidates.length == 0)
    {
      return PsiNamedElement.EMPTY_ARRAY;
    }

    // Add everything resolved
    PsiElement[] psiElements = ResolveUtil.mapToElements(candidates);
    variants.addAll(Arrays.asList(mapToLookupItems(psiElements)));

    // Add Java methods for all imported classes
    boolean withoutDot = mayBeMethodReference(symbol);
    if (symbol.getChildren().length == 0 && symbol.getText().startsWith(".") || withoutDot)
    {
      addJavaMethods(psiElements, variants, withoutDot);
    }

    return variants.toArray(new Object[variants.size()]);
  }

  private static boolean mayBeMethodReference(SchemeIdentifier symbol)
  {
    PsiElement parent = symbol.getParent();
    if (parent == null)
    {
      return false;
    }

    //    if (parent.getParent() instanceof SchemeList && ".".equals(((SchemeList) parent.getParent()).getHeadText())) return true;
    return false;
  }

  private static LookupItem[] mapToLookupItems(PsiElement[] elements)
  {
    List<LookupItem> list = ContainerUtil.map(elements, new Function<PsiElement, LookupItem>()
    {
      public LookupItem fun(PsiElement element)
      {
        LookupItem
          item =
          new LookupItem<PsiElement>(element,
                                     element instanceof PsiNamedElement ?
                                     ((PsiNamedElement) element).getName() :
                                     element.toString());
        //        if (element instanceof ClDef)
        //        {
        //          ClDef def = (ClDef) element;
        //          item.setTailType(TailType.SPACE);
        //          item.setAttribute(LookupItem.TAIL_TEXT_ATTR, " " + def.getParameterString());
        //          item.setAttribute(LookupItem.TYPE_TEXT_ATTR, def.getContainingFile().getName());
        //        }
        return item;
      }
    });
    return list.toArray(LookupItem.EMPTY_ARRAY);
  }

  private static void addJavaMethods(PsiElement[] psiElements, Collection<Object> variants, boolean withoutDot)
  {
    HashMap<MethodSignature, HashSet<PsiMethod>> sig2Methods = collectAvailableMethods(psiElements);

    for (Map.Entry<MethodSignature, HashSet<PsiMethod>> entry : sig2Methods.entrySet())
    {
      MethodSignature sig = entry.getKey();
      String name = sig.getName();

      StringBuffer buffer = new StringBuffer();
      buffer.append(name).append("(");
      buffer.append(StringUtil.join(ContainerUtil.map2Array(sig.getParameterTypes(),
                                                            String.class,
                                                            new Function<PsiType, String>()
                                                            {
                                                              public String fun(PsiType psiType)
                                                              {
                                                                return psiType.getPresentableText();
                                                              }
                                                            }), ", ")).append(")");

      String methodText = buffer.toString();

      StringBuffer tailBuffer = new StringBuffer();
      tailBuffer.append(" in ");
      ArrayList<String> list = new ArrayList<String>();
      for (PsiMethod method : entry.getValue())
      {
        PsiClass clazz = method.getContainingClass();
        if (clazz != null)
        {
          list.add(clazz.getQualifiedName());
        }
      }
      tailBuffer.append(StringUtil.join(list, ", "));

      LookupItem item = new LookupItem(methodText, (!withoutDot ? "." : "") + name);
      item.setIcon(SchemeIcons.JAVA_METHOD);
      item.setTailText(tailBuffer.toString(), true);

      variants.add(item);
    }
  }

  public static HashMap<MethodSignature, HashSet<PsiMethod>> collectAvailableMethods(PsiElement[] psiElements)
  {
    HashMap<MethodSignature, HashSet<PsiMethod>> sig2Methods = new HashMap<MethodSignature, HashSet<PsiMethod>>();
    for (PsiElement element : psiElements)
    {
      if (element instanceof PsiClass)
      {
        PsiClass clazz = (PsiClass) element;
        if (clazz.getName().equals("Pattern"))
        {
          System.out.println("");
        }
        for (PsiMethod method : clazz.getAllMethods())
        {
          if (!method.isConstructor() && method.hasModifierProperty(PsiModifier.PUBLIC))
          {
            MethodSignature sig = method.getSignature(PsiSubstitutor.EMPTY);
            HashSet<PsiMethod> set = sig2Methods.get(sig);
            if (set == null)
            {
              HashSet<PsiMethod> newSet = new HashSet<PsiMethod>();
              newSet.add(method);
              sig2Methods.put(sig, newSet);
            }
            else
            {
              set.add(method);
            }
          }
        }
      }
    }
    return sig2Methods;
  }

}