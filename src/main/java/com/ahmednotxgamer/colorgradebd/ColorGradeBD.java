package com.ahmednotxgamer.colorgradebd;

import com.ahmednotxgamer.colorgradebd.client.keybind.KeybindManager;
import com.ahmednotxgamer.colorgradebd.client.render.ColorGradingRenderer;
import com.ahmednotxgamer.colorgradebd.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class ColorGradeBD implements ClientModInitializer {

    public static final String MOD_ID = "colorgradebd";
    public static final String MOD_NAME = "ColorGrade BD";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static ColorGradeBD instance;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[ColorGrade BD] Initializing...");

        // Load config first
        ConfigManager.getInstance().load();

        // Register keybinds
        KeybindManager.getInstance().register();

        // Initialize renderer
        ColorGradingRenderer.getInstance().initialize();

        LOGGER.info("[ColorGrade BD] Initialized successfully.");
    }

    public static ColorGradeBD getInstance() {
        return instance;
    }
}
