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
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

// https://github.com/jaredlll08/Controlling/blob/10c04497a6bc182ba2788f84ffbbac21da8390bc/buildSrc/src/main/kotlin/com/blamejared/controlling/gradle/DefaultPlugin.kt#L71
public class PKSubprojPlugin implements Plugin<Project> {
  private SubprojExtension cfg;
  private PKExtension rootCfg;

  private String archivesBaseName;

  @Override
  public void apply(Project project) {
    this.cfg = project.getExtensions().create("pkSubproj", SubprojExtension.class);

    project.afterEvaluate(this::setupReal);
  }

  private void setupReal(Project project) {
    this.rootCfg = project.getRootProject().getExtensions().getByType(PKExtension.class);
    var modInfo = this.rootCfg.getModInfo();

    if (this.rootCfg.superDebugInfo) {
      project.getLogger().warn(modInfo.toString());
      project.getLogger().warn(this.cfg.toString());
    }

    if (this.rootCfg.doProjectMetadata) {
      project.setGroup("at.petra-k." + modInfo.getModID());
      String ver = this.getFullVersionString(project);
      project.setVersion(ver);
      project.setProperty("archivesBaseName",
          this.archivesBaseName = modInfo.getModID());
    }

    if (this.rootCfg.setupJarMetadata) {
      this.configJava(project);
    }
    if (this.rootCfg.setupMavenMetadata) {
      this.configMaven(project);
    }

    project.getPlugins().apply(CurseForgeGradlePlugin.class);
    project.getPlugins().apply(Minotaur.class);

    var changelog = MiscUtil.getMostRecentPush(project.getRootProject());
    var isRelease = MiscUtil.isRelease(changelog);

    project.getTasks().register("publishCurseForge", TaskPublishCurseForge.class,
            t -> this.setupCurseforge(t, changelog))
        .configure(t -> {
          t.onlyIf($ -> isRelease && this.cfg.publish);
        });
    this.setupModrinth(project, changelog);

    project.getTasks().register("publishModrinth", TaskModrinthUpload.class).configure(t -> {
      t.onlyIf($ -> isRelease && this.cfg.publish);
    });
  }

  private void configJava(Project project) {
    var modInfo = this.rootCfg.getModInfo();

    {
      var java = project.getExtensions().getByType(JavaPluginExtension.class);
      java.getToolchain().getLanguageVersion().set(JavaLanguageVersion.of(21));
      java.withSourcesJar();
      java.withJavadocJar();
    }

    project.getTasks().withType(JavaCompile.class).configureEach(it -> {
      it.getOptions().setEncoding("UTF-8");
      it.getOptions().getRelease().set(this.rootCfg.javaVersion);
    });

    // Setup jar
    project.getTasks().named("jar", Jar.class).configure(jar -> {
      jar.manifest(mani -> {
        // not Map.of to catch NPE on the right line
        var attrs = new HashMap<String, Object>();
        attrs.put("Specification-Title", modInfo.getModID());
        attrs.put("Specification-Vendor", "petra-kat");
        attrs.put("Specification-Version", jar.getArchiveVersion().get());
        attrs.put("Implementation-Title", project.getName());
        attrs.put("Implementation-Version", jar.getArchiveVersion().get());
        attrs.put("Implementation-Vendor", "petra-kat");
        // i hate time
        attrs.put("Implementation-Timestamp",
            LocalDateTime.now()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)));
        attrs.put("Timestampe", System.currentTimeMillis());
        attrs.put("Built-On-Java",
            System.getProperty("java.vm.version") + " " + System.getProperty("java.vm.vendor"));
        attrs.put("Build-On-Minecraft", modInfo.getMcVersion());

        mani.attributes(attrs);
      });

      if (this.rootCfg.superDebugInfo) {
        project.getLogger().warn("Jar manifest for {}:", jar.getArchiveFileName().get());
        jar.getManifest().getAttributes().forEach((k, v) ->
            project.getLogger().warn("  {} : {}", k, v));
      }
    });
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
          if (rootCfg.superDebugInfo) {
            project.getLogger().warn("Removing dep: {}", dep);
          }
          dep.getParentNode().removeChild(dep);
        }
        if (rootCfg.superDebugInfo) {
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

    var mainJar = this.cfg.curseforgeJar;
    var mainUpload = task.upload(userCfg.getId(), mainJar);

    mainUpload.addGameVersion(rootCfg.getModInfo().getMcVersion());
    mainUpload.addJavaVersion("Java " + rootCfg.javaVersion);

    mainUpload.releaseType = userCfg.getStability();

    for (var dep : this.cfg.getCurseforgeDependencies()) {
      mainUpload.addRequirement(dep);
    }
    mainUpload.addModLoader(this.cfg.platform);

    mainUpload.changelog = "# " + changelog;
    mainUpload.changelogType = net.darkhax.curseforgegradle.Constants.CHANGELOG_MARKDOWN;
  }

  private void setupModrinth(Project project, String changelog) {
    var modrinthExt = project.getExtensions().getByType(ModrinthExtension.class);

    var userCfg = rootCfg.getModrinthInfo();

    modrinthExt.getToken().set(userCfg.getToken());
    modrinthExt.getUploadFile().set(this.cfg.modrinthJar);
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

  private String getFullVersionString(Project project) {
    var changelog = MiscUtil.getRawGitChangelogList(project);
    var info = this.rootCfg.getModInfo();

    String version = info.getModVersion();
    if (!MiscUtil.isRelease(changelog) && System.getenv("BUILD_NUMBER") != null) {
      version += "-pre-" + System.getenv("BUILD_NUMBER");
    }
    // semver babay
    version += "+%s-%s".formatted(this.cfg.platform, info.getMcVersion());

    if (System.getenv("TAG_NAME") != null) {
      version = System.getenv("TAG_NAME").substring(1);
      project.getLogger().info("Version overridden to tag version " + version);
    }

    return version;
  }
}
