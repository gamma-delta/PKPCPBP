package at.petrak.pkpcpbp.cfg;

import java.util.List;

public class CurseforgeInfoExtension {
    int id;
    String stability;

    List<String> dependencies;

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
    public void dependencies(List<String> deps) {
        this.dependencies = deps;
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }
}
