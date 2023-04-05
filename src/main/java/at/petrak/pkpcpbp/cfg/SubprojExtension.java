package at.petrak.pkpcpbp.cfg;

import org.gradle.api.Action;

public class SubprojExtension {
    private final ModInfoExtension modInfo = new ModInfoExtension();

    private String platform;

    public ModInfoExtension getModInfo() {
        return modInfo;
    }

    public void modInfo(Action<? super ModInfoExtension> cfg) {
        cfg.execute(this.modInfo);
    }

    public String getPlatform() {
        return platform;
    }

    public void platform(String platform) {
        this.platform = platform;
    }
}
