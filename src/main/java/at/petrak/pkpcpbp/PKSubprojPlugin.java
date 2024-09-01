package at.petrak.pkpcpbp;

import at.petrak.pkpcpbp.cfg.PKExtension;
import at.petrak.pkpcpbp.cfg.SubprojExtension;
import com.modrinth.minotaur.Minotaur;
import com.modrinth.minotaur.ModrinthExtension;
import com.modrinth.minotaur.TaskModrinthUpload;
import com.modrinth.minotaur.dependencies.Dependency;
import com.modrinth.minotaur.dependencies.DependencyType;
import com.modrinth.minotaur.dependencies.VersionDependency;
import net.darkhax.curseforgegradle.CurseForgeGradlePlugin;
import net.darkhax.curseforgegradle.TaskPublishCurseForge;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

// https://github.com/jaredlll08/Controlling/blob/10c04497a6bc182ba2788f84ffbbac21da8390bc/buildSrc/src/main/kotlin/com/blamejared/controlling/gradle/DefaultPlugin.kt#L71
public class PKSubprojPlugin implements Plugin<Project> {
  private SubprojExtension cfg;
  private PKExtension rootCfg;

  private String archivesBaseName;

  @Override
  public void apply(Project project) {
    this.cfg = project.getExtensions().create("pkSubproj", SubprojExtension.class);
    {
      var java = project.getExtensions().getByType(JavaPluginExtension.class);
      java.getToolchain().getLanguageVersion().set(JavaLanguageVersion.of(21));
      java.withSourcesJar();
      java.withJavadocJar();
    }

    project.afterEvaluate(this::setupReal);
  }

  private void setupReal(Project project) {
    this.rootCfg = project.getRootProject().getExtensions().getByType(PKExtension.class);
    var modInfo = this.rootCfg.getModInfo();

    if (this.rootCfg.getSuperDebugInfo()) {
      project.getLogger().warn(modInfo.toString());
      project.getLogger().warn(this.cfg.toString());
    }

    if (this.rootCfg.getDoProjectMetadata()) {
      project.setGroup("at.petra-k." + modInfo.getModID());
      project.setVersion(MiscUtil.getVersion(project, modInfo));
      project.setProperty("archivesBaseName", this.archivesBaseName =
          "%s-%s-%s".formatted(modInfo.getModID(), cfg.getPlatform(), modInfo.getMcVersion()));
    }

    if (this.rootCfg.getSetupMavenMetadata()) {
      this.configMaven(project);
    }


    if (this.cfg.getPublish()) {
      project.getPlugins().apply(CurseForgeGradlePlugin.class);
      project.getPlugins().apply(Minotaur.class);

      var changelog = MiscUtil.getMostRecentPush(project.getRootProject());
      var isRelease = MiscUtil.isRelease(changelog);

      project.getTasks().register("publishCurseForge", TaskPublishCurseForge.class,
              t -> this.setupCurseforge(t, changelog))
          .configure(t -> {
            t.onlyIf($ -> isRelease);
          });
      this.setupModrinth(project, changelog);

      project.getTasks().register("publishModrinth", TaskModrinthUpload.class).configure(t -> {
        t.onlyIf($ -> isRelease);
      });
    }
  }

  private void configMaven(Project project) {
    var publishing = project.getExtensions().getByType(PublishingExtension.class);

    // Disables Gradle's custom module metadata from being published to maven. The
    // metadata includes mapped dependencies which are not reasonably consumable by
    // other mod developers.
    project.getTasks().withType(GenerateModuleMetadata.class).configureEach(it -> {
      it.setEnabled(false);
    });

    publishing.getPublications().register("mavenJava", MavenPublication.class, pub -> {
      pub.setArtifactId(this.archivesBaseName);
      pub.from(project.getComponents().getByName("java"));
      pub.getPom().withXml(xmlProvider -> {
        var xml = xmlProvider.asElement();
        NodeList found;
        try {
          found = (NodeList) XPathFactory.newInstance().newXPath().evaluate("//dependencies/*", xml,
              XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
          throw new RuntimeException(e);
        }
        for (int i = 0; i < found.getLength(); i++) {
          var dep = found.item(i);
          if (rootCfg.getSuperDebugInfo()) {
            project.getLogger().warn("Removing dep: {}", dep);
          }
          dep.getParentNode().removeChild(dep);
        }
        if (rootCfg.getSuperDebugInfo()) {
          project.getLogger().warn("Final XML: {}", xml);
        }
      });
    });

    publishing.repositories(it -> it.maven(maven -> {
      try {
        maven.setUrl(new URL("file:///" + System.getenv("local_maven")));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }));
  }

  private void setupCurseforge(TaskPublishCurseForge task, String changelog) {
    var userCfg = rootCfg.getCfInfo();

    task.apiToken = userCfg.getToken();

    var mainJar = this.cfg.getCurseforgeJar();
    var mainUpload = task.upload(userCfg.getId(), mainJar);

    mainUpload.addGameVersion(rootCfg.getModInfo().getMcVersion());
    // can't WAIT for me to forget about this when java 18 rolls around
    mainUpload.addJavaVersion("Java 17");

    mainUpload.releaseType = userCfg.getStability();

    for (var dep : this.cfg.getCurseforgeDependencies()) {
      mainUpload.addRequirement(dep);
    }
    mainUpload.addModLoader(this.cfg.getPlatform());

    mainUpload.changelog = "# " + changelog;
    mainUpload.changelogType = net.darkhax.curseforgegradle.Constants.CHANGELOG_MARKDOWN;
  }

  private void setupModrinth(Project project, String changelog) {
    var modrinthExt = project.getExtensions().getByType(ModrinthExtension.class);

    var userCfg = rootCfg.getModrinthInfo();

    modrinthExt.getToken().set(userCfg.getToken());
    modrinthExt.getUploadFile().set(this.cfg.getModrinthJar());
    modrinthExt.getProjectId().set(userCfg.getId());

    modrinthExt.getVersionNumber().set(this.rootCfg.getModInfo().getModVersion());
    modrinthExt.getVersionName().set(this.archivesBaseName);
    modrinthExt.getVersionType().set(userCfg.getStability());

    var deps = new ArrayList<Dependency>();
    for (var s : this.cfg.getModrinthDependencies()) {
      var split = s.split(":");
      var id = split[0];
      var version = split[1];
      var ty =
          split.length == 2
              ? DependencyType.REQUIRED
              : DependencyType.valueOf(split[2]);
      deps.add(new VersionDependency(id, version, ty));
    }
    modrinthExt.getDependencies().addAll(deps);
    modrinthExt.getChangelog().set("# " + changelog);
  }
}
