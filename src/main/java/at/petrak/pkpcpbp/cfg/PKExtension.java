package at.petrak.pkpcpbp.cfg;

import org.gradle.api.Action;

public class PKExtension {
    private final ModInfoExtension modInfo = new ModInfoExtension();
    private final CurseforgeInfoExtension cfInfo = new CurseforgeInfoExtension();
    private final ModrinthInfoExtension modrinthInfo = new ModrinthInfoExtension();
    private boolean superDebugInfo = false;

    public void modInfo(Action<? super ModInfoExtension> cfg) {
        cfg.execute(this.modInfo);
    }

    public ModInfoExtension getModInfo() {
        return modInfo;
    }

    public void curseforgeInfo(Action<? super CurseforgeInfoExtension> cfg) {
        cfg.execute(this.cfInfo);
    }

    public CurseforgeInfoExtension getCfInfo() {
        return this.cfInfo;
    }

    public void modrinthInfo(Action<? super ModrinthInfoExtension> cfg) {
        cfg.execute(this.modrinthInfo);
    }

    public ModrinthInfoExtension getModrinthInfo() {
        return this.modrinthInfo;
    }

    public void superDebugInfo(boolean b) {
        this.superDebugInfo = b;
    }

    public boolean getSuperDebugInfo() {
        return this.superDebugInfo;
    }
}
