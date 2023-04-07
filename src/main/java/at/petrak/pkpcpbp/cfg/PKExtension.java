package at.petrak.pkpcpbp.cfg;

import org.gradle.api.Action;

public class PKExtension {
    private final ModInfoExtension modInfo = new ModInfoExtension();
    private boolean superDebugInfo;

    public void modInfo(Action<? super ModInfoExtension> cfg) {
        cfg.execute(this.modInfo);
    }

    public ModInfoExtension getModInfo() {
        return modInfo;
    }

    public void superDebugInfo(boolean b) {
        this.superDebugInfo = b;
    }

    public boolean getSuperDebugInfo() {
        return this.superDebugInfo;
    }

    @Override
    public String toString() {
        return "PKExtension{" +
            "modInfo=" + modInfo +
            ", superDebugInfo=" + superDebugInfo +
            '}';
    }
}
