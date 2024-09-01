package at.petrak.pkpcpbp.cfg;

public class ModInfoExtension {
  public String modID;
  public String mcVersion;
  public String modVersion;

  @Override
  public String toString() {
    return "ModInfoExtension{" +
        "modID='" + modID + '\'' +
        ", mcVersion='" + mcVersion + '\'' +
        ", modVersion='" + modVersion + '\'' +
        '}';
  }
}
