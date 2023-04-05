package at.petrak.pkpcpbp.cfg;

public class ModInfoExtension {

    private String modID;
    private String mcVersion;
    private String modVersion;

    public String getModID() {
        return modID;
    }

    public void modID(String modID) {
        this.modID = modID;
    }

    public String getMcVersion() {
        return mcVersion;
    }

    public void mcVersion(String mcVersion) {
        this.mcVersion = mcVersion;
    }

    public String getModVersion() {
        return modVersion;
    }

    public void modVersion(String modVersion) {
        this.modVersion = modVersion;
    }

    @Override
    public String toString() {
        return "ModInfoExtension{" +
            "modID='" + modID + '\'' +
            ", mcVersion='" + mcVersion + '\'' +
            ", modVersion='" + modVersion + '\'' +
            '}';
    }
}
