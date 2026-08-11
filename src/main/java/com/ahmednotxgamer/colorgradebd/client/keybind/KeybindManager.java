package com.ahmednotxgamer.colorgradebd.client.keybind;

import com.ahmednotxgamer.colorgradebd.client.gui.screens.ColorGradeScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Registers and handles the configurable keybind for opening ColorGrade BD.
 * Appears in Minecraft's Controls screen under the "ColorGrade BD" category.
 */
public class KeybindManager {

    private static final KeybindManager INSTANCE = new KeybindManager();

    private KeyBinding openGuiKey;

    private KeybindManager() {}

    public static KeybindManager getInstance() {
        return INSTANCE;
    }

    public void register() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.colorgradebd.open_gui",          // translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F7,                     // default key: F7
                "category.colorgradebd"               // category translation key
        ));

        // Check each tick if the key was pressed
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ColorGradeScreen(null));
                }
            }
        });
    }

    public KeyBinding getOpenGuiKey() {
        return openGuiKey;
    }
}
