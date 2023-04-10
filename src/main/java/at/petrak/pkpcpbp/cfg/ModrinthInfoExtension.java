package at.petrak.pkpcpbp.cfg;

public class ModrinthInfoExtension {
    String id;
    String stability;

    public void id(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void stability(String stability) {
        this.stability = stability;
    }

    public String getStability() {
        return this.stability;
    }
}
