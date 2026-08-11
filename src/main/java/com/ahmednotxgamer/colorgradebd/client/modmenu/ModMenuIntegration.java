package com.ahmednotxgamer.colorgradebd.client.modmenu;

import com.ahmednotxgamer.colorgradebd.client.gui.screens.ColorGradeScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Registers ColorGrade BD's configuration screen with Mod Menu.
 * Clicking "Configure" in Mod Menu opens the same ColorGradeScreen
 * used by the keybind, so there is only one config system.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ColorGradeScreen(parent);
    }
}
