package industries.angelic.tabfaces.compat.angellib;

import net.minecraft.util.ResourceLocation;

/**
 * Bridge for optional AngelLib integration.
 * <p>
 * Keep all AngelLib references in this class so normal TabFaces classes do not
 * try to load AngelLib classes when the mod is absent.
 */
public class AngelLibCompat {

    public static void drawFace(ResourceLocation resourceLocation, float xPos, float yPos, float alpha) {
        industries.angelic.angellib.util.ClientUtil.drawPlayerFace(resourceLocation, xPos, yPos, alpha, 8);
    }
}
