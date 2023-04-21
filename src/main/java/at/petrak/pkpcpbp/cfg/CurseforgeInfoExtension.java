package at.petrak.pkpcpbp.cfg;

public class CurseforgeInfoExtension {
    String token;

    int id;
    String stability;

    public void token(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

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
}
