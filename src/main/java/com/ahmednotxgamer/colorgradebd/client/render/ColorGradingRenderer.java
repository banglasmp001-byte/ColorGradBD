package com.ahmednotxgamer.colorgradebd.client.render;

import com.ahmednotxgamer.colorgradebd.ColorGradeBD;
import com.ahmednotxgamer.colorgradebd.config.ConfigManager;
import com.ahmednotxgamer.colorgradebd.config.GlobalColorSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ColorGradingRenderer {

    private static final ColorGradingRenderer INSTANCE = new ColorGradingRenderer();
    private static final Identifier SHADER_ID = Identifier.of("colorgradebd", "post/colorgrade");

    private PostEffectProcessor postEffect = null;
    private boolean shaderLoaded = false;
    private boolean shaderLoadFailed = false;
    private int lastWidth = -1;
    private int lastHeight = -1;

    private float lastBrightness = Float.NaN;
    private float lastContrast   = Float.NaN;
    private float lastSaturation = Float.NaN;
    private float lastHue        = Float.NaN;
    private float lastSharpness  = Float.NaN;
    private float lastColorR     = Float.NaN;
    private float lastColorG     = Float.NaN;
    private float lastColorB     = Float.NaN;
    private float lastIntensity  = Float.NaN;
    private float lastGamma      = Float.NaN;
    private float lastTemperature = Float.NaN;
    private float lastVignette   = Float.NaN;

    private ColorGradingRenderer() {}

    public static ColorGradingRenderer getInstance() { return INSTANCE; }

    public void initialize() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> applyPostProcessing());
    }

    public void applyPostProcessing() {
        GlobalColorSettings settings = ConfigManager.getInstance().getGlobal();
        if (!settings.enabled) {
            if (shaderLoaded) unloadShader();
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getWindow() == null) return;

        int w = mc.getWindow().getFramebufferWidth();
        int h = mc.getWindow().getFramebufferHeight();

        if (!shaderLoaded && !shaderLoadFailed) {
            loadShader(mc, w, h);
        }
        if (!shaderLoaded || postEffect == null) return;

        if (w != lastWidth || h != lastHeight) {
            onWindowResized(w, h);
        }

        pushUniforms(settings);
        postEffect.render(mc.getRenderTickCounter().getLastFrameDuration());
    }

    private void loadShader(MinecraftClient mc, int w, int h) {
        try {
            if (postEffect != null) { postEffect.close(); postEffect = null; }
            postEffect = PostEffectProcessor.loadEffect(
                    mc.getResourceManager(), SHADER_ID,
                    mc.getFramebuffer(), null);
            if (postEffect != null) {
                lastWidth  = w;
                lastHeight = h;
                shaderLoaded = true;
                shaderLoadFailed = false;
                ColorGradeBD.LOGGER.info("[ColorGrade BD] Shader loaded ({}x{})", w, h);
            }
        } catch (Exception e) {
            shaderLoadFailed = true;
            shaderLoaded = false;
            ColorGradeBD.LOGGER.error("[ColorGrade BD] Shader load failed: {}", e.getMessage());
        }
    }

    private void unloadShader() {
        if (postEffect != null) { postEffect.close(); postEffect = null; }
        shaderLoaded = false;
        resetCachedUniforms();
    }

    private void pushUniforms(GlobalColorSettings s) {
        if (postEffect == null) return;
        boolean changed = s.brightness != lastBrightness || s.contrast != lastContrast
                || s.saturation != lastSaturation || s.hue != lastHue
                || s.sharpness != lastSharpness || s.colorR != lastColorR
                || s.colorG != lastColorG || s.colorB != lastColorB
                || s.intensity != lastIntensity || s.gamma != lastGamma
                || s.temperature != lastTemperature || s.vignette != lastVignette;
        if (!changed) return;

        lastBrightness = s.brightness; lastContrast = s.contrast;
        lastSaturation = s.saturation; lastHue = s.hue;
        lastSharpness  = s.sharpness;  lastColorR = s.colorR;
        lastColorG     = s.colorG;     lastColorB = s.colorB;
        lastIntensity  = s.intensity;  lastGamma = s.gamma;
        lastTemperature = s.temperature; lastVignette = s.vignette;

        try {
            postEffect.getPrograms().forEach(pass -> {
                trySetUniform(pass, "Brightness",   s.brightness);
                trySetUniform(pass, "Contrast",     s.contrast);
                trySetUniform(pass, "Saturation",   s.saturation);
                trySetUniform(pass, "HueShift",     s.hue / 180.0f);
                trySetUniform(pass, "Sharpness",    s.sharpness);
                trySetUniform(pass, "ColorR",       s.colorR);
                trySetUniform(pass, "ColorG",       s.colorG);
                trySetUniform(pass, "ColorB",       s.colorB);
                trySetUniform(pass, "Intensity",    s.intensity);
                trySetUniform(pass, "Gamma",        s.gamma);
                trySetUniform(pass, "Temperature",  s.temperature);
                trySetUniform(pass, "Vignette",     s.vignette);
            });
        } catch (Exception e) {
            ColorGradeBD.LOGGER.debug("[ColorGrade BD] Uniform push error: {}", e.getMessage());
        }
    }

    private void trySetUniform(net.minecraft.client.gl.PostEffectPass pass,
                               String name, float value) {
        try {
            var u = pass.getProgram().getUniformByName(name);
            if (u != null) u.set(value);
        } catch (Exception ignored) {}
    }

    private void resetCachedUniforms() {
        lastBrightness = lastContrast = lastSaturation = lastHue =
        lastSharpness  = lastColorR   = lastColorG    = lastColorB =
        lastIntensity  = lastGamma    = lastTemperature = lastVignette = Float.NaN;
    }

    public void onWindowResized(int width, int height) {
        lastWidth  = width;
        lastHeight = height;
        if (shaderLoaded && postEffect != null) {
            try {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null) {
                    unloadShader();
                    shaderLoadFailed = false;
                }
            } catch (Exception e) {
                ColorGradeBD.LOGGER.debug("[ColorGrade BD] Resize error: {}", e.getMessage());
            }
        }
    }

    public void markDirty()           { resetCachedUniforms(); }
    public boolean isShaderLoaded()   { return shaderLoaded; }
    public void resetShaderFailFlag() { shaderLoadFailed = false; }
}
