package at.petrak.pkpcpbp;

import at.petrak.pkpcpbp.cfg.PKExtension;
import at.petrak.pkpcpbp.cfg.SubprojExtension;
import at.petrak.pkpcpbp.filters.FlatteningJson5Transmogrifier;
import at.petrak.pkpcpbp.filters.Json5Transmogrifier;
import net.darkhax.curseforgegradle.TaskPublishCurseForge;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

// https://github.com/jaredlll08/Controlling/blob/10c04497a6bc182ba2788f84ffbbac21da8390bc/buildSrc/src/main/kotlin/com/blamejared/controlling/gradle/DefaultPlugin.kt#L71
public class PKSubprojPlugin implements Plugin<Project> {
    private SubprojExtension cfg;
    private PKExtension rootCfg;

    @Override
    public void apply(Project project) {
        this.cfg = project.getExtensions().create("pkSubproj", SubprojExtension.class);
        {
            var java = project.getExtensions().getByType(JavaPluginExtension.class);
            java.getToolchain().getLanguageVersion().set(JavaLanguageVersion.of(17));
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

        project.setGroup("at.petra-k." + modInfo.getModID());
        project.setVersion(MiscUtil.getVersion(project, modInfo));
        project.setProperty("archivesBaseName",
            "%s-%s-%s".formatted(modInfo.getModID(), cfg.getPlatform(), modInfo.getMcVersion()));

        this.configJava(project);
        this.configMaven(project);

        // Disables Gradle's custom module metadata from being published to maven. The
        // metadata includes mapped dependencies which are not reasonably consumable by
        // other mod developers.
        project.getTasks().withType(GenerateModuleMetadata.class).configureEach(it -> {
            it.setEnabled(false);
        });

        project.getTasks().withType(ProcessResources.class).configureEach(it -> {
            // always make it redo
            it.getOutputs().upToDateWhen($ -> false);
            it.exclude(".cache");

            it.filesMatching(List.of("assets/**/*.flatten.json5", "data/**/*.flatten.json5"), file -> {
                file.setPath(file.getPath().replace(".flatten.json5", ".json"));
                file.filter(FlatteningJson5Transmogrifier.class);
            });

            it.filesMatching(List.of("assets/**/*.json5", "data/**/*.json5"), file -> {
                file.setPath(file.getPath().replace(".json5", ".json"));
                file.filter(Json5Transmogrifier.class);
            });
        });

        if (this.cfg.getPublish()) {
            var changelog = MiscUtil.getGitChangelog(project.getRootProject());
            project.getTasks().register("publishCurseForge", TaskPublishCurseForge.class,
                t -> this.setupCurseforge(t, changelog));
        }
    }

    private void configJava(Project project) {
        var modInfo = this.rootCfg.getModInfo();

        project.getTasks().withType(JavaCompile.class).configureEach(it -> {
            it.getOptions().setEncoding("UTF-8");
            it.getOptions().getRelease().set(17);
        });

        // Setup jar
        project.getTasks().withType(Jar.class).configureEach(jar -> {
            jar.getArchiveVersion().set(project.getVersion().toString());
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

                if (this.rootCfg.getSuperDebugInfo()) {
                    project.getLogger().warn("Jar manifest:");
                    attrs.forEach((k, v) -> project.getLogger().warn("%s : %s".formatted(k, v)));
                }

                mani.attributes(attrs);
            });
        });
    }

    private void configMaven(Project project) {
        var publishing = project.getExtensions().getByType(PublishingExtension.class);

        var base = project.getExtensions().getByType(BasePluginExtension.class);
        publishing.getPublications().register("mavenJava", MavenPublication.class, pub -> {
            pub.setArtifactId(base.getArchivesName().get());
            pub.from(project.getComponents().getByName("java"));
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
        if (!MiscUtil.isRelease(changelog)) {
            return;
        }
        var cf = rootCfg.getCfInfo();

        task.apiToken = System.getProperty("curseforge_token");

        var mainJar = getJarByName("jar", task);
        var mainUpload = task.upload(cf.getId(), mainJar);

        mainUpload.addGameVersion(rootCfg.getModInfo().getMcVersion());
        // can't WAIT for me to forget about this when java 18 rolls around
        mainUpload.addJavaVersion("Java 17");

        mainUpload.releaseType = cf.getStability();

        for (var dep : cf.getDependencies()) {
            mainUpload.addRequirement(dep);
        }
        mainUpload.addModLoader(this.cfg.getPlatform());

        mainUpload.changelog = "# " + changelog;
        mainUpload.changelogType = net.darkhax.curseforgegradle.Constants.CHANGELOG_MARKDOWN;
    }

    private static File getJarByName(String name, Task task) {
        return task.getProject().getTasks().named(name, Jar.class).get().getArchiveFile().get().getAsFile();
    }
}
