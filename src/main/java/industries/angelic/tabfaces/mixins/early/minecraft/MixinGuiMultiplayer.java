package industries.angelic.tabfaces.mixins.early.minecraft;

import java.util.List;

import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import industries.angelic.tabfaces.Config;
import industries.angelic.tabfaces.util.ClientUtil;

@SuppressWarnings("unused")
@Mixin(GuiMultiplayer.class)
public abstract class MixinGuiMultiplayer extends GuiScreen {

    @Shadow
    private ServerSelectionList field_146803_h;

    @Shadow
    private ServerList field_146804_i;

    @SuppressWarnings("unused")
    @WrapOperation(
        method = "drawScreen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiMultiplayer;func_146283_a(Ljava/util/List;II)V"))
    private void wrapTooltipRendering(GuiMultiplayer instance, List<String> lines, int mouseX, int mouseY,
        Operation<Void> original) {
        if (Config.enableFacesInServerMenu) {
            ServerData hoveredServer = findHoveredServerData(lines);
            if (hoveredServer != null) {
                ClientUtil.drawHoveringTextWithFaces(this, lines, mouseX, mouseY, hoveredServer);
                return;
            }
        }

        original.call(instance, lines, mouseX, mouseY);
    }

    /**
     * Find the ServerData whose player tooltip text matches the displayed lines.
     */
    private ServerData findHoveredServerData(List<String> lines) {
        if (field_146804_i == null || lines == null || lines.isEmpty()) {
            return null;
        }

        String tooltipText = joinLines(lines);

        for (int i = 0; i < field_146804_i.countServers(); i++) {
            ServerData sd = field_146804_i.getServerData(i);
            if (sd != null && tooltipText.equals(sd.field_147412_i)) {
                return sd;
            }
        }

        return null;
    }

    private static String joinLines(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(lines.get(i));
        }
        return sb.toString();
    }
}
