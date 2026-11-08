package goblinbob.mobends.core;

public class ModStatics
{
    public static final String MODID = "mobends";
    public static final String NAME = "Mo' Bends";
    public static final String LOADER = "forge";
    public static final String VERSION = "2.0.0-alpha.2";
    public static final String MCVERSION = "1.12.2";
    /**
     * Read by Forge's version checker. On 1.12.2 Forge reads it only from the {@code @Mod} annotation;
     * an {@code updateJSON} entry in mcmod.info is ignored.
     */
    public static final String UPDATE_JSON_URL = "https://mobends.com/mod-update.json";

    public static final String VERSION_STRING = VERSION + "/" + MCVERSION + "-" + LOADER;
}
