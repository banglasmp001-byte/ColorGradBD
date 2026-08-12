package com.ahmednotxgamer.colorgradebd.client.render;

import com.ahmednotxgamer.colorgradebd.ColorGradeBD;
import com.ahmednotxgamer.colorgradebd.config.ConfigManager;
import com.ahmednotxgamer.colorgradebd.config.GlobalColorSettings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

@Environment(EnvType.CLIENT)
public class ColorGradingRenderer {

    private static final ColorGradingRenderer INSTANCE = new ColorGradingRenderer();

    private ColorGradingRenderer() {}

    public static ColorGradingRenderer getInstance() { return INSTANCE; }

    public void initialize() {
        // 1.21.1 correct signature: DrawContext + RenderTickCounter
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        GlobalColorSettings s = ConfigManager.getInstance().getGlobal();
        if (!s.enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Brightness — white overlay (brighten) or black overlay (darken)
        if (s.brightness > 0.005f) {
            int a = Math.min(220, (int)(s.brightness * 200 * s.intensity));
            context.fill(0, 0, sw, sh, argb(255, 255, 255, a));
        } else if (s.brightness < -0.005f) {
            int a = Math.min(240, (int)(-s.brightness * 220 * s.intensity));
            context.fill(0, 0, sw, sh, argb(0, 0, 0, a));
        }

        // Temperature — warm orange or cool blue tint
        if (s.temperature > 0.02f) {
            int a = Math.min(90, (int)(s.temperature * 80 * s.intensity));
            context.fill(0, 0, sw, sh, argb(255, 140, 30, a));
        } else if (s.temperature < -0.02f) {
            int a = Math.min(90, (int)(-s.temperature * 80 * s.intensity));
            context.fill(0, 0, sw, sh, argb(30, 100, 255, a));
        }

        // Saturation — grey overlay desaturates
        if (s.saturation < -0.02f) {
            int a = Math.min(190, (int)(-s.saturation * 170 * s.intensity));
            context.fill(0, 0, sw, sh, argb(120, 120, 120, a));
        }

        // Color tint RGB multipliers
        float rd = Math.abs(s.colorR - 1f);
        float gd = Math.abs(s.colorG - 1f);
        float bd = Math.abs(s.colorB - 1f);
        if (rd + gd + bd > 0.05f) {
            int tr = s.colorR > 1f ? 255 : (int)(s.colorR * 200);
            int tg = s.colorG > 1f ? 255 : (int)(s.colorG * 200);
            int tb = s.colorB > 1f ? 255 : (int)(s.colorB * 200);
            int ta = Math.min(90, (int)((rd + gd + bd) * 45f * s.intensity));
            if (ta > 2) context.fill(0, 0, sw, sh, argb(tr, tg, tb, ta));
        }

        // Contrast low = grey wash, high = nothing (no darkening overlay needed)
        if (s.contrast < -0.02f) {
            int a = Math.min(110, (int)(-s.contrast * 90 * s.intensity));
            context.fill(0, 0, sw, sh, argb(128, 128, 128, a));
        }

        // Hue tint
        if (Math.abs(s.hue) > 5f) {
            float[] rgb = hueToRgb(s.hue);
            int a = Math.min(60, (int)(Math.abs(s.hue) / 180f * 50f * s.intensity));
            if (a > 2) context.fill(0, 0, sw, sh,
                    argb((int)(rgb[0]*255), (int)(rgb[1]*255), (int)(rgb[2]*255), a));
        }

        // Gamma > 1 = lighten, < 1 = darken midtones
        if (s.gamma > 1.05f) {
            int a = Math.min(100, (int)((s.gamma - 1f) * 90 * s.intensity));
            context.fill(0, 0, sw, sh, argb(255, 255, 210, a));
        } else if (s.gamma < 0.95f) {
            int a = Math.min(130, (int)((1f - s.gamma) * 110 * s.intensity));
            context.fill(0, 0, sw, sh, argb(10, 10, 30, a));
        }

        // Vignette — dark border gradient
        if (s.vignette > 0.01f) {
            int steps  = 14;
            int border = (int)(Math.min(sw, sh) * 0.55f);
            for (int i = 0; i < steps; i++) {
                float t  = (float) i / steps;
                int   a  = Math.min(240, (int)(s.vignette * 220 * s.intensity * t * t));
                int   thick = Math.max(1, border / steps);
                int   p  = i * thick;
                int   col = argb(0, 0, 0, a);
                context.fill(p,        p,        sw - p,      p + thick,  col);
                context.fill(p,        sh-p-thick,sw - p,     sh - p,     col);
                context.fill(p,        p,        p + thick,   sh - p,     col);
                context.fill(sw-p-thick,p,       sw - p,      sh - p,     col);
            }
        }

        RenderSystem.disableBlend();
    }

    private int argb(int r, int g, int b, int a) {
        return (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private int clamp(int v) { return Math.min(255, Math.max(0, v)); }

    private float[] hueToRgb(float deg) {
        float h = ((deg % 360) + 360) % 360;
        int   i = (int)(h / 60f) % 6;
        float f = h / 60f - (int)(h / 60f);
        float v = 1f, p = 0f, q = 1f - f, t = f;
        return switch (i) {
            case 0  -> new float[]{v, t, p};
            case 1  -> new float[]{q, v, p};
            case 2  -> new float[]{p, v, t};
            case 3  -> new float[]{p, q, v};
            case 4  -> new float[]{t, p, v};
            default -> new float[]{v, p, q};
        };
    }

    public void markDirty()           {}
    public boolean isShaderLoaded()   { return true; }
    public void resetShaderFailFlag() {}
    public void onWindowResized(int w, int h) {}
}
