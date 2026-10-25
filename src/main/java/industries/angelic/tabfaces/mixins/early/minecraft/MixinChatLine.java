package industries.angelic.tabfaces.mixins.early.minecraft;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import industries.angelic.tabfaces.access.IMixinChatLine;

@SuppressWarnings("unused")
@Mixin(ChatLine.class)
public class MixinChatLine implements IMixinChatLine {

    @Shadow
    @Final
    IChatComponent lineString;

    @Override
    public IChatComponent getLineString() {
        return lineString;
    }
}
