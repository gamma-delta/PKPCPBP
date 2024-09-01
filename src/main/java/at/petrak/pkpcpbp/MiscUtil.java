package at.petrak.pkpcpbp;

import org.gradle.api.Project;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

public class MiscUtil {
  public static String getRawGitChangelogList(Project project) {
    var stdout = new ByteArrayOutputStream();
    var gitHash = System.getenv("GIT_COMMIT");
    var gitPrevHash = System.getenv("GIT_PREVIOUS_COMMIT");
    var travisRange = System.getenv("TRAVIS_COMMIT_RANGE");

    if (gitHash != null && gitPrevHash != null) {
      project.exec(spec -> {
        spec.commandLine("git", "log", "--pretty=tformat:- %s", gitPrevHash + "..." + gitHash);
        spec.setStandardOutput(stdout);
      });
    } else if (travisRange != null) {
      project.exec(spec -> {
        spec.commandLine("git", "log", "--pretty=tformat:- %s", travisRange);
        spec.setStandardOutput(stdout);
      });
    } else {
      return "";
    }

    return stdout.toString();
  }

  public static String getMostRecentPush(Project project) {
    var stdout = new ByteArrayOutputStream();

    project.exec(spec -> {
      spec.commandLine("git", "log", "--pretty=tformat:%s", "HEAD~..HEAD");
      spec.setStandardOutput(stdout);
    });

    return stdout.toString();
  }

  public static boolean isRelease(String changelog) {
    Pattern pat = Pattern.compile("^\\[release", Pattern.CASE_INSENSITIVE);
    return pat.asPredicate().test(changelog);
  }
}