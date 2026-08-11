package com.ahmednotxgamer.colorgradebd.config;

/**
 * Per-element color grading settings.
 * Each element identified by its registry key (e.g. "minecraft:stone")
 * carries its own independent set of grading parameters.
 */
public class ElementColorSettings {

    // Element registry key, e.g. "minecraft:stone"
    public String registryKey = "";

    // Display name for the UI
    public String displayName = "";

    // Whether this element's grading is active
    public boolean enabled = true;

    // Brightness: -1.0 to 1.0
    public float brightness = 0.0f;

    // Contrast: -1.0 to 1.0
    public float contrast = 0.0f;

    // Saturation: -1.0 to 1.0
    public float saturation = 0.0f;

    // Hue shift: -180.0 to 180.0
    public float hue = 0.0f;

    // Sharpness: 0.0 to 1.0
    public float sharpness = 0.0f;

    // Color tint RGB multiplier: 0.0 to 1.0 each
    public float colorR = 1.0f;
    public float colorG = 1.0f;
    public float colorB = 1.0f;

    // Intensity/strength: 0.0 to 1.0
    public float intensity = 1.0f;

    // Depth/3D effect strength: 0.0 to 1.0
    public float depthEffect = 0.0f;

    public ElementColorSettings() {}

    public ElementColorSettings(String registryKey, String displayName) {
        this.registryKey = registryKey;
        this.displayName = displayName;
    }

    public ElementColorSettings copy() {
        ElementColorSettings copy = new ElementColorSettings();
        copy.registryKey = this.registryKey;
        copy.displayName = this.displayName;
        copy.enabled = this.enabled;
        copy.brightness = this.brightness;
        copy.contrast = this.contrast;
        copy.saturation = this.saturation;
        copy.hue = this.hue;
        copy.sharpness = this.sharpness;
        copy.colorR = this.colorR;
        copy.colorG = this.colorG;
        copy.colorB = this.colorB;
        copy.intensity = this.intensity;
        copy.depthEffect = this.depthEffect;
        return copy;
    }

    public void resetToDefault() {
        enabled = true;
        brightness = 0.0f;
        contrast = 0.0f;
        saturation = 0.0f;
        hue = 0.0f;
        sharpness = 0.0f;
        colorR = 1.0f;
        colorG = 1.0f;
        colorB = 1.0f;
        intensity = 1.0f;
        depthEffect = 0.0f;
    }

    /** Returns true if all values are at their defaults (no grading effect). */
    public boolean isAtDefault() {
        return brightness == 0.0f && contrast == 0.0f && saturation == 0.0f
                && hue == 0.0f && sharpness == 0.0f
                && colorR == 1.0f && colorG == 1.0f && colorB == 1.0f
                && intensity == 1.0f && depthEffect == 0.0f;
    }
}
