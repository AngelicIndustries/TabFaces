package industries.angelic.tabfaces.mixins.early.minecraft;

import net.minecraft.client.gui.Gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import industries.angelic.tabfaces.access.IMixinGui;

@SuppressWarnings("unused")
@Mixin(Gui.class)
public class MixinGui implements IMixinGui {

    @Shadow
    float zLevel;

    @Shadow
    protected void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {}

    @Override
    public void setZLevel(float level) {
        zLevel = level;
    }

    @Override
    public void drawGradientRectPub(int left, int top, int right, int bottom, int startColor, int endColor) {
        drawGradientRect(left, top, right, bottom, startColor, endColor);
    }
}
