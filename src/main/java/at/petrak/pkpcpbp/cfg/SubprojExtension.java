package at.petrak.pkpcpbp.cfg;

public class SubprojExtension {
    private final ModInfoExtension modInfo = new ModInfoExtension();

    private String platform;

    public ModInfoExtension getModInfo() {
        return modInfo;
    }

    public String getPlatform() {
        return platform;
    }
}
