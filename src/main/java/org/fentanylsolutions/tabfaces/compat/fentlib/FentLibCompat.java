package org.fentanylsolutions.tabfaces.compat.fentlib;

import net.minecraft.util.ResourceLocation;

/**
 * Bridge for optional FentLib integration.
 * <p>
 * Keep all FentLib references in this class so normal TabFaces classes do not
 * try to load FentLib classes when the mod is absent.
 */
public class FentLibCompat {

    public static void drawFace(ResourceLocation resourceLocation, float xPos, float yPos, float alpha) {
        org.fentanylsolutions.fentlib.util.ClientUtil.drawPlayerFace(resourceLocation, xPos, yPos, alpha, 8);
    }
}
