package at.petrak.pkpcpbp.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SubprojExtension {
  public String platform;
  // apparently `publish` has special meaning to groovy?
  public boolean pkPublish = false;

  // only used if rootCfg.doProjectMetadata is false
  public String artifactId;
  public String versionDisplayName;

  public Object curseforgeJar;
  public Object modrinthJar;

  private List<String> cfDeps = new ArrayList<>();
  private List<String> modrinthDeps = new ArrayList<>();

  /**
   * The slugs of the dependencies
   */
  public void curseforgeDependencies(Collection<Object> deps) {
    for (var it : deps) {
      this.cfDeps.add(it.toString());
    }
  }

  public void curseforgeDependency(Object dep) {
    this.cfDeps.add(dep.toString());
  }

  public List<String> getCurseforgeDependencies() {
    return this.cfDeps;
  }

  public void modrinthDependencies(Collection<Object> deps) {
    // oh fuck off groovy
    for (var it : deps) {
      this.modrinthDeps.add(it.toString());
    }
  }

  public void modrinthDependency(Object dep) {
    this.cfDeps.add(dep.toString());
  }

  public List<String> getModrinthDependencies() {
    return this.modrinthDeps;
  }

  @Override
  public String toString() {
    return "SubprojExtension{" +
        "platform='" + platform + '\'' +
        ", publish=" + pkPublish +
        ", cfJar=" + curseforgeJar +
        ", modrinthJar=" + modrinthJar +
        ", cfDeps=" + cfDeps +
        ", modrinthDeps=" + modrinthDeps +
        '}';
  }
}

