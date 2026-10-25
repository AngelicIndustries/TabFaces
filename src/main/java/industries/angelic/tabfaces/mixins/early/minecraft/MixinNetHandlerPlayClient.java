package industries.angelic.tabfaces.mixins.early.minecraft;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import industries.angelic.tabfaces.TabFaces;

@SuppressWarnings("unused")
@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Inject(method = "handleSpawnPlayer", at = @At("HEAD"))
    private void tabfaces$captureSpawnProfile(S0CPacketSpawnPlayer packetIn, CallbackInfo ci) {
        GameProfile profile = packetIn.func_148948_e();
        TabFaces.varInstanceClient.clientRegistry.insertProfile(profile, false, -1);
    }
}
