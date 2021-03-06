package schemely.config;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jdom.Element;
import schemely.config.ui.SchemeFacetTab;


@State(
  name = "SchemeFacetConfiguration",
  storages = {@Storage(
    id = "default",
    file = "$MODULE_FILE$")})
public class SchemeFacetConfiguration implements FacetConfiguration,
                                                 PersistentStateComponent<SchemeLibrariesConfiguration>
{
  private final SchemeLibrariesConfiguration librariesConfiguration = new SchemeLibrariesConfiguration();

  public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager)
  {
    return new FacetEditorTab[]{new SchemeFacetTab(editorContext, librariesConfiguration)};
  }

  public void readExternal(Element element) throws InvalidDataException
  {
  }

  public void writeExternal(Element element) throws WriteExternalException
  {
  }

  public SchemeLibrariesConfiguration getState()
  {
    return librariesConfiguration;
  }

  public void loadState(SchemeLibrariesConfiguration state)
  {
    XmlSerializerUtil.copyBean(state, librariesConfiguration);
  }
}
