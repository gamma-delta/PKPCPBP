package at.petrak.pkpcpbp.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SubprojExtension {
    private String platform;
    private Boolean publish = null;

    private Object cfJar;
    private Object modrinthJar;

    private List<String> cfDeps = new ArrayList<>();
    private List<String> modrinthDeps = new ArrayList<>();


    public String getPlatform() {
        return platform;
    }

    public void platform(String platform) {
        this.platform = platform;
    }

    public void publish(boolean publish) {
        this.publish = publish;
    }

    public boolean getPublish() {
        if (this.publish == null) {
            return "forge".equals(this.platform) || "fabric".equals(this.platform);
        } else {
            return this.publish;
        }
    }

    public void curseforgeJar(Object jar) {
        this.cfJar = jar;
    }

    public Object getCurseforgeJar() {
        return this.cfJar;
    }

    public void modrinthJar(Object jar) {
        this.modrinthJar = jar;
    }

    public Object getModrinthJar() {
        return this.modrinthJar;
    }

    /**
     * The slugs of the dependencies
     */
    public void curseforgeDependencies(Collection<Object> deps) {
        for (var it : deps) {
            this.cfDeps.add(it.toString());
        }
    }

    public void curseforgeDependency(Object dep) {
        this.cfDeps.add(dep.toString());
    }

    public List<String> getCurseforgeDependencies() {
        return this.cfDeps;
    }

    public void modrinthDependencies(Collection<Object> deps) {
        // oh fuck off groovy
        for (var it : deps) {
            this.modrinthDeps.add(it.toString());
        }
    }

    public void modrinthDependency(Object dep) {
        this.cfDeps.add(dep.toString());
    }

    public List<String> getModrinthDependencies() {
        return this.modrinthDeps;
    }

    @Override
    public String toString() {
        return "SubprojExtension{" +
            "platform='" + platform + '\'' +
            ", publish=" + publish +
            ", cfJar=" + cfJar +
            ", modrinthJar=" + modrinthJar +
            ", cfDeps=" + cfDeps +
            ", modrinthDeps=" + modrinthDeps +
            '}';
    }
}

