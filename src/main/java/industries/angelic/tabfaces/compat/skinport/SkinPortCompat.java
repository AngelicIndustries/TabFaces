package industries.angelic.tabfaces.compat.skinport;

import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;

import lain.mods.skinport.init.forge.ClientProxy;
import lain.mods.skins.api.SkinProviderAPI;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.impl.PlayerProfile;

public class SkinPortCompat {

    public static ResourceLocation getSkin(GameProfile profile) {
        if (profile == null || profile.getId() == null) {
            return null;
        }

        ISkin skin = SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(profile));
        if (skin == null || !skin.isDataReady()) {
            return null;
        }

        return ClientProxy.getOrCreateTexture(skin.getData(), skin)
            .getLocation();
    }
}
