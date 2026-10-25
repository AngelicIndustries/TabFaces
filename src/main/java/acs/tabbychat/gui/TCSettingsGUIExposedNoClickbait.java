package acs.tabbychat.gui;

import net.minecraft.client.gui.GuiButton;

import acs.tabbychat.core.TabbyChat;
import industries.angelic.tabfaces.Config;
import industries.angelic.tabfaces.access.IMixinTCSettingSlider;
import industries.angelic.tabfaces.access.IMixinTabbyChat;
import industries.angelic.tabfaces.compat.tabbychat.TabFacesSettings;

public class TCSettingsGUIExposedNoClickbait extends TCSettingsGUI {

    public TCSettingsGUIExposedNoClickbait(TabbyChat _tc) {
        super(_tc);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.id == TabFacesSettings.TABFACES_SETTINGS_SAVE_BUTTON_ID) {
            Config.getRawConfig()
                .get(Config.Categories.chat, "enableFacesInTabbyChat", true)
                .set(((IMixinTabbyChat) tc).getTabFacesSettings().showPlayerFaces.getTempValue());
            Config.enableFacesInTabbyChat = ((IMixinTabbyChat) tc).getTabFacesSettings().showPlayerFaces.getTempValue();

            IMixinTCSettingSlider slider = (IMixinTCSettingSlider) ((IMixinTabbyChat) tc)
                .getTabFacesSettings().faceXOffsetSlider;
            int val = Math
                .round(slider.getSliderValue() * (slider.getMaxValue() - slider.getMinValue()) + slider.getMinValue());
            Config.getRawConfig()
                .get(Config.Categories.chat, "faceXOffsetTabbyChat", 1)
                .set(val);
            Config.faceXOffsetTabbyChat = val;

            Config.getRawConfig()
                .save();
        }
    }
}
