package com.ahmednotxgamer.colorgradebd.client.render;

import com.ahmednotxgamer.colorgradebd.ColorGradeBD;
import com.ahmednotxgamer.colorgradebd.config.ConfigManager;
import com.ahmednotxgamer.colorgradebd.config.GlobalColorSettings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ColorGradingRenderer {

    private static final ColorGradingRenderer INSTANCE = new ColorGradingRenderer();

    private ColorGradingRenderer() {}

    public static ColorGradingRenderer getInstance() { return INSTANCE; }

    public void initialize() {
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onHudRender(net.minecraft.client.gui.DrawContext context, float tickDelta) {
        GlobalColorSettings s = ConfigManager.getInstance().getGlobal();
        if (!s.enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Brightness
        if (s.brightness > 0.005f) {
            int a = (int)(s.brightness * 180 * s.intensity);
            context.fill(0, 0, sw, sh, toArgb(255, 255, 255, Math.min(200, a)));
        } else if (s.brightness < -0.005f) {
            int a = (int)(-s.brightness * 200 * s.intensity);
            context.fill(0, 0, sw, sh, toArgb(0, 0, 0, Math.min(220, a)));
        }

        // Temperature
        if (s.temperature > 0.02f) {
            int a = (int)(s.temperature * 60 * s.intensity);
            context.fill(0, 0, sw, sh, toArgb(255, 160, 50, Math.min(80, a)));
        } else if (s.temperature < -0.02f) {
            int a = (int)(-s.temperature * 60 * s.intensity);
            context.fill(0, 0, sw, sh, toArgb(50, 120, 255, Math.min(80, a)));
        }

        // Color tint RGB
        float tintDiff = Math.abs(s.colorR - 1f) + Math.abs(s.colorG - 1f) + Math.abs(s.colorB - 1f);
        if (tintDiff > 0.02f) {
            int tr = s.colorR > 1f ? 255 : (int)(s.colorR * 255);
            int tg = s.colorG > 1f ? 255 : (int)(s.colorG * 255);
            int tb = s.colorB > 1f ? 255 : (int)(s.colorB * 255);
            int ta = (int)(tintDiff * 40f * s.intensity);
            context.fill(0, 0, sw, sh, toArgb(tr, tg, tb, Math.min(80, ta)));
        }

        // Saturation reduce = grey overlay
        if (s.saturation < -0.02f) {
            int a = (int)(-s.saturation * 160 * s.intensity);
            context.fill(0, 0, sw, sh, toArgb(128, 128, 128, Math.min(180, a)));
        }

        // Vignette
        if (s.vignette > 0.01f) {
            int steps = 12;
            int border = (int)(Math.min(sw, sh) * 0.5f * s.vignette);
            for (int i = 0; i < steps; i++) {
                float t = (float) i / steps;
                int a = (int)(s.vignette * 200 * s.intensity * t * t);
                int thick = Math.max(1, border / steps);
                int p = i * thick;
                int color = toArgb(0, 0, 0, Math.min(230, a));
                context.fill(p, p, sw - p, p + thick, color);
                context.fill(p, sh - p - thick, sw - p, sh - p, color);
                context.fill(p, p, p + thick, sh - p, color);
                context.fill(sw - p - thick, p, sw - p, sh - p, color);
            }
        }

        // Contrast low = grey wash
        if (s.contrast < -0.02f) {
            int a = (int)(-s.contrast * 80 * s.intensity);
            context.fill(0, 0, sw, sh, toArgb(128, 128, 128, Math.min(100, a)));
        }

        // Hue tint
        if (Math.abs(s.hue) > 5f) {
            float[] rgb = hueToRgb(s.hue);
            int a = (int)(Math.abs(s.hue) / 180f * 45f * s.intensity);
            context.fill(0, 0, sw, sh, toArgb(
                    (int)(rgb[0]*255), (int)(rgb[1]*255), (int)(rgb[2]*255), Math.min(55, a)));
        }

        RenderSystem.disableBlend();
    }

    private int toArgb(int r, int g, int b, int a) {
        return (Math.min(255,Math.max(0,a)) << 24)
             | (Math.min(255,Math.max(0,r)) << 16)
             | (Math.min(255,Math.max(0,g)) << 8)
             |  Math.min(255,Math.max(0,b));
    }

    private float[] hueToRgb(float deg) {
        float h = ((deg % 360) + 360) % 360;
        int i = (int)(h / 60f) % 6;
        float f = h/60f - (int)(h/60f);
        float v=1f,p=0f,q=1f-f,t=f;
        return switch(i){
            case 0->new float[]{v,t,p};
            case 1->new float[]{q,v,p};
            case 2->new float[]{p,v,t};
            case 3->new float[]{p,q,v};
            case 4->new float[]{t,p,v};
            default->new float[]{v,p,q};
        };
    }

    public void markDirty() {}
    public boolean isShaderLoaded() { return true; }
    public void resetShaderFailFlag() {}
    public void onWindowResized(int w, int h) {}
}
