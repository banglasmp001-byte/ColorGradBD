package com.ahmednotxgamer.colorgradebd.config;

/**
 * Holds all global post-processing color grading settings.
 * All float values use normalized ranges appropriate for each property.
 */
public class GlobalColorSettings {

    // Global on/off switch
    public boolean enabled = false;

    // Brightness: -1.0 to 1.0 (default 0.0 = no change)
    public float brightness = 0.0f;

    // Contrast: -1.0 to 1.0 (default 0.0 = no change)
    public float contrast = 0.0f;

    // Saturation: -1.0 to 1.0 (default 0.0 = no change)
    public float saturation = 0.0f;

    // Hue shift: -180.0 to 180.0 degrees (default 0.0 = no change)
    public float hue = 0.0f;

    // Sharpness: 0.0 to 1.0 (default 0.0 = no sharpening)
    public float sharpness = 0.0f;

    // Color tint RGB (applied as multiplier overlay): 0.0 to 1.0 each
    public float colorR = 1.0f;
    public float colorG = 1.0f;
    public float colorB = 1.0f;

    // Overall intensity/strength of the grading effect: 0.0 to 1.0 (default 1.0 = full)
    public float intensity = 1.0f;

    // Gamma: 0.5 to 2.0 (default 1.0 = no change)
    public float gamma = 1.0f;

    // Temperature: -1.0 to 1.0 (default 0.0 = neutral)
    public float temperature = 0.0f;

    // Vignette: 0.0 to 1.0 (default 0.0 = off)
    public float vignette = 0.0f;

    public GlobalColorSettings copy() {
        GlobalColorSettings copy = new GlobalColorSettings();
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
        copy.gamma = this.gamma;
        copy.temperature = this.temperature;
        copy.vignette = this.vignette;
        return copy;
    }

    public void resetToDefault() {
        enabled = false;
        brightness = 0.0f;
        contrast = 0.0f;
        saturation = 0.0f;
        hue = 0.0f;
        sharpness = 0.0f;
        colorR = 1.0f;
        colorG = 1.0f;
        colorB = 1.0f;
        intensity = 1.0f;
        gamma = 1.0f;
        temperature = 0.0f;
        vignette = 0.0f;
    }
}
