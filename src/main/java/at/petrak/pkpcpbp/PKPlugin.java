/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package at.petrak.pkpcpbp;

import at.petrak.pkpcpbp.cfg.PKExtension;
import com.diluv.schoomp.Webhook;
import com.diluv.schoomp.message.Message;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class PKPlugin implements Plugin<Project> {
  private boolean isRelease = false;
  private String changelog = "";

  private PKExtension cfg;

  public void apply(Project project) {
    this.cfg = project.getExtensions().create("pkpcpbp", PKExtension.class);

    project.afterEvaluate(this::applyReal);
  }

  private void applyReal(Project project) {
    if (this.cfg.getSuperDebugInfo()) {
      project.getLogger().warn(this.cfg.toString());
    }

    this.changelog = MiscUtil.getRawGitChangelogList(project);
    this.isRelease = MiscUtil.isRelease(this.changelog);
//        project.setVersion(MiscUtil.getVersion(project, this.cfg.getModInfo()));

    project.task("publishToDiscord", t -> t.doLast(this::pushWebhook));
  }


  private void pushWebhook(Task task) {
    try {
      String discordWebhook = System.getenv("discordWebhook");
      String buildUrl = System.getenv("BUILD_URL");
      if (discordWebhook == null || buildUrl == null) {
        task.getLogger().warn("Cannot send the webhook without the webhook url or the build url");
        return;
      }
      var webhook = new Webhook(discordWebhook, "Petrak@ Patreon Gradle");

      var message = new Message();
      message.setUsername("Patreon Early Access");
      message.setContent("""
          New `%s` prerelease -- build #%s for %s!
          Download it here: %s
          Changelog: ```
          %s
          ```"""
          .formatted(this.cfg.getModInfo().getModID(),
              System.getenv("BUILD_NUMBER"),
              cfg.getModInfo().getMcVersion(),
              buildUrl,
              this.changelog));

      webhook.sendMessage(message);
    } catch (Exception exn) {
      task.getLogger().error("Failed to push Discord webhook.", exn);
    }
  }
}
