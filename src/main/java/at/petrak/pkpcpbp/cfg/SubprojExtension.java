package at.petrak.pkpcpbp.cfg;

public class SubprojExtension {
    private String platform;


    public String getPlatform() {
        return platform;
    }

    public void platform(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "SubprojExtension{" +
            "platform='" + platform + '\'' +
            '}';
    }
}
