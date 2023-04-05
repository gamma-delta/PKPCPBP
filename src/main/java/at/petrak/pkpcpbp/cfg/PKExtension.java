package at.petrak.pkpcpbp.cfg;

public class PKExtension {
    private ModInfoExtension modInfo = new ModInfoExtension();

    public void modInfo(ModInfoExtension modInfo) {
        this.modInfo = modInfo;
    }

    public ModInfoExtension getModInfo() {
        return modInfo;
    }
}
