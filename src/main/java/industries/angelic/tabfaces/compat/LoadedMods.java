package industries.angelic.tabfaces.compat;

import cpw.mods.fml.common.Loader;

public class LoadedMods {

    public static final String SKINPORT_MODID = "skinport";
    public static final String SIMPLESKINBACKPORT_MODID = "simpleskinbackport";
    public static final String ANGELLIB_MODID = "angellib";
    public static final String WAWELAUTH_MODID = "wawelauth";
    public static final String CUSTOM_SKIN_LOADER_MODID = "customskinloader";

    public static boolean skinPortLoaded = false;
    public static boolean simpleSkinBackportLoaded = false;
    public static boolean angelLibLoaded = false;
    public static boolean wawelAuthLoaded = false;
    public static boolean customSkinLoaderLoaded = false;

    public static void init() {
        skinPortLoaded = Loader.isModLoaded(SKINPORT_MODID);
        simpleSkinBackportLoaded = Loader.isModLoaded(SIMPLESKINBACKPORT_MODID);
        angelLibLoaded = Loader.isModLoaded(ANGELLIB_MODID);
        wawelAuthLoaded = Loader.isModLoaded(WAWELAUTH_MODID);
        customSkinLoaderLoaded = Loader.isModLoaded(CUSTOM_SKIN_LOADER_MODID);
    }
}
