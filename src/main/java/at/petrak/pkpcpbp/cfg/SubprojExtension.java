package at.petrak.pkpcpbp.cfg;

public class SubprojExtension {
    private ModInfoExtension modInfo = new ModInfoExtension();

    private String platform;

    public ModInfoExtension getModInfo() {
        return modInfo;
    }

    public void modInfo(ModInfoExtension modInfo) {
        this.modInfo = modInfo;
    }

    public String getPlatform() {
        return platform;
    }

    public void platform(String platform) {
        this.platform = platform;
    }
}
