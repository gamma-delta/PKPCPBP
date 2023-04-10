package at.petrak.pkpcpbp.cfg;

public class SubprojExtension {
    private String platform;
    private Boolean publish = null;


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

    @Override
    public String toString() {
        return "SubprojExtension{" +
            "platform='" + platform + '\'' +
            ", publish=" + publish +
            '}';
    }
}

