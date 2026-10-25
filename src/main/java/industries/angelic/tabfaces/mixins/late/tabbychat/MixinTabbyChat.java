package industries.angelic.tabfaces.mixins.late.tabbychat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import acs.tabbychat.core.GuiNewChatTC;
import acs.tabbychat.core.TabbyChat;
import industries.angelic.tabfaces.access.IMixinTabbyChat;
import industries.angelic.tabfaces.compat.tabbychat.TabFacesSettings;

@SuppressWarnings("unused")
@Mixin(value = TabbyChat.class, remap = false)
public class MixinTabbyChat implements IMixinTabbyChat {

    private static TabFacesSettings tabFacesSettings;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(GuiNewChatTC gncInstance, CallbackInfo ci) {
        tabFacesSettings = new TabFacesSettings((TabbyChat) (Object) this);
    }

    @Override
    public TabFacesSettings getTabFacesSettings() {
        return tabFacesSettings;
    }
}
