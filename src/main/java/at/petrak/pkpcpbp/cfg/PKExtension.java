package at.petrak.pkpcpbp.cfg;

import org.gradle.api.Action;

public class PKExtension {
    private final ModInfoExtension modInfo = new ModInfoExtension();

    public void modInfo(Action<? super ModInfoExtension> cfg) {
        cfg.execute(this.modInfo);
    }

    public ModInfoExtension getModInfo() {
        return modInfo;
    }
}
