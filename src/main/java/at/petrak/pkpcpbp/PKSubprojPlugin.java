package at.petrak.pkpcpbp;

import at.petrak.pkpcpbp.cfg.ModInfoExtension;
import at.petrak.pkpcpbp.cfg.PKExtension;
import at.petrak.pkpcpbp.cfg.SubprojExtension;
import at.petrak.pkpcpbp.filters.FlatteningJson5Transmogrifier;
import at.petrak.pkpcpbp.filters.Json5Transmogrifier;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

// https://github.com/jaredlll08/Controlling/blob/10c04497a6bc182ba2788f84ffbbac21da8390bc/buildSrc/src/main/kotlin/com/blamejared/controlling/gradle/DefaultPlugin.kt#L71
public class PKSubprojPlugin implements Plugin<Project> {
    private SubprojExtension cfg;
    private ModInfoExtension modInfo;

    @Override
    public void apply(Project proj) {
        this.cfg = proj.getExtensions().create("pkSubproj", SubprojExtension.class);
        proj.afterEvaluate(this::setupReal);
    }

    private void setupReal(Project project) {
        this.modInfo = project.getParent().getExtensions().getByType(PKExtension.class).getModInfo();
        project.getLogger().warn(this.cfg.toString());

        project.setGroup("at.petra-k." + this.modInfo.getModID());
        project.setVersion(MiscUtil.getVersion(project, this.modInfo));
        project.setProperty("archivesBaseName",
            "%s-%s-%s".formatted(this.modInfo.getModID(), cfg.getPlatform(), this.modInfo.getMcVersion()));

        this.configJava(project);
        this.configDependencies(project);
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
    }

    private void configJava(Project project) {
        {
            var java = project.getExtensions().getByType(JavaPluginExtension.class);
            java.toolchain(it -> it.getLanguageVersion().set(JavaLanguageVersion.of(17)));
            java.withSourcesJar();
            java.withJavadocJar();
        }

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
                attrs.put("Specification-Title", this.modInfo.getModID());
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
                attrs.put("Build-On-Minecraft", this.modInfo.getMcVersion());

                mani.attributes(attrs);
            });
        });
    }

    private void configDependencies(Project project) {
        var implementation = project.getConfigurations().getByName("implementation");
        var compileOnly = project.getConfigurations().getByName("compileOnly");
        var annotationProcessor = project.getConfigurations().getByName("annotationProcessor");

        implementation.getDependencies().add(project.getDependencies().create("org.jetbrains:annotations:24.0.1"));
    }

    private void configMaven(Project project) {
        var publishing = project.getExtensions().getByType(PublishingExtension.class);

        project.afterEvaluate(it -> {
            var base = project.getExtensions().getByType(BasePluginExtension.class);
            publishing.getPublications().register("mavenJava", MavenPublication.class, pub -> {
                pub.setArtifactId(base.getArchivesName().get());
                pub.from(project.getComponents().getByName("java"));
            });
        });

        publishing.repositories(it ->
            it.maven(maven -> maven.artifactUrls("file:///" + System.getenv("local_maven"))));
    }
}
