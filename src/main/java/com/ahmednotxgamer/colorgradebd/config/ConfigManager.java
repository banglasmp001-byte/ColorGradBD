package com.ahmednotxgamer.colorgradebd.config;

import com.ahmednotxgamer.colorgradebd.ColorGradeBD;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Handles loading and saving of {@link ColorGradeConfig} to/from disk as JSON.
 * Uses a singleton pattern; thread-safety for config access is caller responsibility
 * (always access from client thread).
 */
public class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "colorgradebd.json";

    private ColorGradeConfig config = new ColorGradeConfig();
    private Path configPath;

    private ConfigManager() {}

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    /** Load config from disk. Falls back to defaults if file is missing or corrupt. */
    public void load() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                ColorGradeConfig loaded = GSON.fromJson(reader, ColorGradeConfig.class);
                if (loaded != null) {
                    config = loaded;
                    // Ensure non-null sub-objects (safe migration from old configs)
                    if (config.global == null) config.global = new GlobalColorSettings();
                    if (config.elements == null) config.elements = new java.util.ArrayList<>();
                    ColorGradeBD.LOGGER.info("[ColorGrade BD] Config loaded from {}", configPath);
                } else {
                    ColorGradeBD.LOGGER.warn("[ColorGrade BD] Config file was empty, using defaults.");
                    config = new ColorGradeConfig();
                }
            } catch (Exception e) {
                ColorGradeBD.LOGGER.error("[ColorGrade BD] Failed to load config, using defaults: {}", e.getMessage());
                config = new ColorGradeConfig();
            }
        } else {
            ColorGradeBD.LOGGER.info("[ColorGrade BD] No config file found, creating default at {}", configPath);
            config = new ColorGradeConfig();
            save(); // Write defaults immediately
        }
    }

    /** Save current config to disk. Safe to call from client thread. */
    public void save() {
        if (configPath == null) {
            configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        }
        try {
            Path parent = configPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            ColorGradeBD.LOGGER.error("[ColorGrade BD] Failed to save config: {}", e.getMessage());
        }
    }

    public ColorGradeConfig getConfig() {
        return config;
    }

    public GlobalColorSettings getGlobal() {
        return config.global;
    }

    /** Find element settings by registry key. Returns empty if not configured. */
    public Optional<ElementColorSettings> findElement(String registryKey) {
        return config.elements.stream()
                .filter(e -> e.registryKey.equals(registryKey))
                .findFirst();
    }

    /** Add or replace element settings. */
    public void putElement(ElementColorSettings settings) {
        config.elements.removeIf(e -> e.registryKey.equals(settings.registryKey));
        config.elements.add(settings);
    }

    /** Remove element settings by registry key. */
    public void removeElement(String registryKey) {
        config.elements.removeIf(e -> e.registryKey.equals(registryKey));
    }

    /** Reset element to default (remove its config). */
    public void resetElement(String registryKey) {
        removeElement(registryKey);
    }
}
