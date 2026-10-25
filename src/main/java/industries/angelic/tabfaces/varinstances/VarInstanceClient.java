package industries.angelic.tabfaces.varinstances;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.minecraft.MinecraftSessionService;

import industries.angelic.tabfaces.TabFaces;
import industries.angelic.tabfaces.registries.ClientRegistry;
import industries.angelic.tabfaces.util.Util;

public class VarInstanceClient {

    public ClientRegistry clientRegistry = new ClientRegistry();
    /*
     * public int u = 0;
     * public int v = 0;
     * public int uWidth = 8;
     * public int vHeight = 8;
     */
    public ResourceLocation defaultResourceLocation = new ResourceLocation(
        TabFaces.MODID,
        Util.useNewSkinFormat() ? "textures/default_square.png" : "textures/default.png");
    public static Minecraft minecraftRef = Minecraft.getMinecraft();
    public static MinecraftSessionService sessionService = minecraftRef.func_152347_ac();
}
