package com.ahmednotxgamer.colorgradebd.client.render;

import com.ahmednotxgamer.colorgradebd.ColorGradeBD;
import com.ahmednotxgamer.colorgradebd.config.ConfigManager;
import com.ahmednotxgamer.colorgradebd.config.GlobalColorSettings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the GPU-side post-processing pipeline for global color grading.
 *
 * Architecture:
 * - Uses Minecraft's built-in PostEffectProcessor (post-process shader chain).
 * - Applies the colorgradebd:post/colorgrade shader after world rendering.
 * - Settings are pushed as shader uniforms; the GPU does the color math.
 * - No CPU-side per-pixel processing.
 * - The pipeline is completely bypassed when the mod is disabled.
 * - The PostEffectProcessor is only (re)loaded when needed, not every frame.
 */
@Environment(EnvType.CLIENT)
public class ColorGradingRenderer {

    private static final ColorGradingRenderer INSTANCE = new ColorGradingRenderer();
    private static final Logger LOGGER = LoggerFactory.getLogger("ColorGrade BD Renderer");

    private static final Identifier SHADER_ID =
            Identifier.of("colorgradebd", "post/colorgrade");

    private PostEffectProcessor postEffect = null;
    private boolean shaderLoaded = false;
    private boolean shaderLoadFailed = false;

    /** Track last-pushed settings to avoid redundant uniform uploads. */
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

    public static ColorGradingRenderer getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        // Register to apply post-processing after world renders.
        // HudRenderCallback fires after world + HUD renders, which gives us
        // access to the finished framebuffer for post-processing.
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            applyPostProcessing();
        });
    }

    /**
     * Called each rendered frame. Applies or bypasses grading based on config.
     * Safe to call every frame: returns immediately if not needed.
     */
    public void applyPostProcessing() {
        GlobalColorSettings settings = ConfigManager.getInstance().getGlobal();

        if (!settings.enabled) {
            // Mod disabled — unload shader if it was loaded, skip processing
            if (shaderLoaded) {
                unloadShader();
            }
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getWindow() == null) return;

        // Lazy-load shader on first use or after window resize
        if (!shaderLoaded && !shaderLoadFailed) {
            loadShader(mc);
        }

        if (!shaderLoaded || postEffect == null) return;

        // Only push uniforms if settings changed (avoids redundant GPU uploads)
        pushUniforms(settings, mc);

        // Resize to current framebuffer if needed
        Framebuffer fb = mc.getFramebuffer();
        postEffect.render(fb, mc.getFramebuffer(), mc.getFramebuffer(),
                mc.getRenderTickCounter());
    }

    private void loadShader(MinecraftClient mc) {
        try {
            if (postEffect != null) {
                postEffect.close();
                postEffect = null;
            }
            postEffect = mc.loadPostEffect(SHADER_ID,
                    java.util.Set.of(mc.getFramebuffer().getColorAttachment()));
            if (postEffect != null) {
                postEffect.setUniformValues("colorgradebd:main");
                int w = mc.getWindow().getFramebufferWidth();
                int h = mc.getWindow().getFramebufferHeight();
                postEffect.setSize(w, h);
                shaderLoaded = true;
                shaderLoadFailed = false;
                LOGGER.info("[ColorGrade BD] Post-process shader loaded ({}x{})", w, h);
            }
        } catch (Exception e) {
            shaderLoadFailed = true;
            shaderLoaded = false;
            LOGGER.error("[ColorGrade BD] Failed to load post-process shader: {}. " +
                    "Color grading will be unavailable.", e.getMessage());
        }
    }

    private void unloadShader() {
        if (postEffect != null) {
            postEffect.close();
            postEffect = null;
        }
        shaderLoaded = false;
        resetCachedUniforms();
        LOGGER.debug("[ColorGrade BD] Post-process shader unloaded (mod disabled).");
    }

    /** Push shader uniforms if any setting changed. */
    private void pushUniforms(GlobalColorSettings s, MinecraftClient mc) {
        if (postEffect == null) return;

        boolean changed = s.brightness   != lastBrightness
                       || s.contrast     != lastContrast
                       || s.saturation   != lastSaturation
                       || s.hue          != lastHue
                       || s.sharpness    != lastSharpness
                       || s.colorR       != lastColorR
                       || s.colorG       != lastColorG
                       || s.colorB       != lastColorB
                       || s.intensity    != lastIntensity
                       || s.gamma        != lastGamma
                       || s.temperature  != lastTemperature
                       || s.vignette     != lastVignette;

        if (!changed) return;

        // Cache new values
        lastBrightness  = s.brightness;
        lastContrast    = s.contrast;
        lastSaturation  = s.saturation;
        lastHue         = s.hue;
        lastSharpness   = s.sharpness;
        lastColorR      = s.colorR;
        lastColorG      = s.colorG;
        lastColorB      = s.colorB;
        lastIntensity   = s.intensity;
        lastGamma       = s.gamma;
        lastTemperature = s.temperature;
        lastVignette    = s.vignette;

        // Push to shader via PostEffectProcessor pass uniforms
        try {
            var passes = postEffect.getPasses();
            if (passes != null && !passes.isEmpty()) {
                var pass = passes.getFirst();
                setUniformIfPresent(pass, "Brightness",   s.brightness);
                setUniformIfPresent(pass, "Contrast",     s.contrast);
                setUniformIfPresent(pass, "Saturation",   s.saturation);
                setUniformIfPresent(pass, "HueShift",     s.hue / 180.0f); // normalize
                setUniformIfPresent(pass, "Sharpness",    s.sharpness);
                setUniformIfPresent(pass, "ColorR",       s.colorR);
                setUniformIfPresent(pass, "ColorG",       s.colorG);
                setUniformIfPresent(pass, "ColorB",       s.colorB);
                setUniformIfPresent(pass, "Intensity",    s.intensity);
                setUniformIfPresent(pass, "Gamma",        s.gamma);
                setUniformIfPresent(pass, "Temperature",  s.temperature);
                setUniformIfPresent(pass, "Vignette",     s.vignette);
            }
        } catch (Exception e) {
            LOGGER.debug("[ColorGrade BD] Could not push shader uniforms: {}", e.getMessage());
        }
    }

    private void setUniformIfPresent(net.minecraft.client.gl.PostEffectPass pass,
                                     String name, float value) {
        try {
            var uniform = pass.getProgram().getUniform(name);
            if (uniform != null) {
                uniform.set(value);
            }
        } catch (Exception ignored) {
            // Uniform may not exist in all shader variants; silently skip
        }
    }

    private void resetCachedUniforms() {
        lastBrightness = lastContrast = lastSaturation = lastHue =
        lastSharpness  = lastColorR   = lastColorG    = lastColorB =
        lastIntensity  = lastGamma    = lastTemperature = lastVignette = Float.NaN;
    }

    /** Call after window resize so the shader resizes too. */
    public void onWindowResized(int width, int height) {
        if (shaderLoaded && postEffect != null) {
            try {
                postEffect.setSize(width, height);
            } catch (Exception e) {
                LOGGER.debug("[ColorGrade BD] Shader resize failed, will reload: {}", e.getMessage());
                unloadShader();
            }
        }
    }

    /** Called when the mod settings are saved — force a uniform re-push next frame. */
    public void markDirty() {
        resetCachedUniforms();
    }

    public boolean isShaderLoaded()     { return shaderLoaded; }
    public boolean isShaderLoadFailed() { return shaderLoadFailed; }
    public void resetShaderFailFlag()   { shaderLoadFailed = false; }
}
