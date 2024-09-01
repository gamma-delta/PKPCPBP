package at.petrak.pkpcpbp;

import at.petrak.pkpcpbp.filters.FlatteningJson5Transmogrifier;
import at.petrak.pkpcpbp.filters.Json5Transmogrifier;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.util.List;

public class PKJson5Plugin implements Plugin<Project> {
  private Cfg cfg;

  @Override
  public void apply(Project project) {
    this.cfg = project.getExtensions().create("pkJson5", Cfg.class);
    project.getTasks().withType(ProcessResources.class).configureEach(it -> {
      if (this.cfg.autoProcessJson5Flattening) {
        it.filesMatching(List.of("assets/**/*.flatten.json5", "data/**/*.flatten.json5"), file -> {
          file.setPath(file.getPath().replace(".flatten.json5", ".json"));
          file.filter(FlatteningJson5Transmogrifier.class);
        });
      }

      if (this.cfg.autoProcessJson5) {
        it.filesMatching(List.of("assets/**/*.json5", "data/**/*.json5"), file -> {
          file.setPath(file.getPath().replace(".json5", ".json"));
          file.filter(Json5Transmogrifier.class);
        });
      }
    });
  }

  public static class Cfg {
    public boolean autoProcessJson5 = true;
    public boolean autoProcessJson5Flattening = true;
  }
}
