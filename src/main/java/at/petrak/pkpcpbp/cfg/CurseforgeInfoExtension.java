package at.petrak.pkpcpbp.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CurseforgeInfoExtension {
    int id;
    String stability;

    List<String> dependencies = new ArrayList<>();

    public void id(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void stability(String stability) {
        this.stability = stability;
    }

    public String getStability() {
        return this.stability;
    }

    /**
     * The slugs of the dependencies
     */
    public void dependencies(Collection<String> deps) {
        this.dependencies.addAll(deps);
    }

    public void dependency(String dep) {
        this.dependencies.add(dep);
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }
}
