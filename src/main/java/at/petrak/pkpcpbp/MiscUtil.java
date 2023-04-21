package at.petrak.pkpcpbp;

import at.petrak.pkpcpbp.cfg.ModInfoExtension;
import org.gradle.api.Project;

import java.io.ByteArrayOutputStream;

public class MiscUtil {
    public static String getVersion(Project project, ModInfoExtension info) {
        var changelog = getRawGitChangelogList(project);

        String version = info.getModVersion();
        if (!isRelease(changelog) && System.getenv("BUILD_NUMBER") != null) {
            version += "-pre-" + System.getenv("BUILD_NUMBER");
        } else if (System.getenv("TAG_NAME") != null) {
            version = System.getenv("TAG_NAME").substring(1);
            project.getLogger().info("Version overridden to tag version " + version);
        }

        return version;
    }

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
        return changelog.matches("(?i)^\\[release");
    }
}