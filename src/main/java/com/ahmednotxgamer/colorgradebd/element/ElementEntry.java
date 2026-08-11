package com.ahmednotxgamer.colorgradebd.element;

/**
 * Represents one selectable entry in the element/block picker.
 * Immutable: only the associated {@link com.ahmednotxgamer.colorgradebd.config.ElementColorSettings}
 * carries mutable grading values.
 */
public final class ElementEntry {

    public enum Category {
        ENVIRONMENT,    // Sky, clouds, fog, etc.
        VANILLA_BLOCK,  // minecraft:* blocks
        MODDED_BLOCK    // Other namespaces
    }

    private final String registryKey;
    private final String displayName;
    private final Category category;

    public ElementEntry(String registryKey, String displayName, Category category) {
        this.registryKey = registryKey;
        this.displayName = displayName;
        this.category = category;
    }

    public String getRegistryKey() { return registryKey; }
    public String getDisplayName() { return displayName; }
    public Category getCategory()  { return category; }

    @Override
    public String toString() {
        return displayName + " [" + registryKey + "]";
    }
}
