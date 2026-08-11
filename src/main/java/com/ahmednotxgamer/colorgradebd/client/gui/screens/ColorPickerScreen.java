package com.ahmednotxgamer.colorgradebd.client.gui.screens;

import com.ahmednotxgamer.colorgradebd.client.gui.widgets.LabeledSliderWidget;
import com.ahmednotxgamer.colorgradebd.config.ElementColorSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Simple vanilla-style RGB color picker for tinting individual elements.
 *
 * Three sliders (R, G, B) + a live preview swatch.
 * Values map directly to ElementColorSettings.colorR/G/B multipliers.
 */
public class ColorPickerScreen extends Screen {

    private final Screen parent;
    private final ElementColorSettings settings;

    private LabeledSliderWidget rSlider;
    private LabeledSliderWidget gSlider;
    private LabeledSliderWidget bSlider;

    // Working copies to support Cancel
    private float workingR;
    private float workingG;
    private float workingB;

    public ColorPickerScreen(Screen parent, ElementColorSettings settings) {
        super(Text.translatable("screen.colorgradebd.color_picker"));
        this.parent   = parent;
        this.settings = settings;
        this.workingR = settings.colorR;
        this.workingG = settings.colorG;
        this.workingB = settings.colorB;
    }

    @Override
    protected void init() {
        super.init();
        int cx      = this.width / 2;
        int sliderW = Math.min(240, this.width - 80);
        int sliderX = cx - sliderW / 2;
        int y       = 60;
        int rowH    = 28;

        // ── R slider ─────────────────────────────────────────────────────────
        rSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.of("Red"),
                0.0, 2.0, workingR,
                v -> workingR = (float) v);
        addDrawableChild(rSlider);
        y += rowH;

        // ── G slider ─────────────────────────────────────────────────────────
        gSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.of("Green"),
                0.0, 2.0, workingG,
                v -> workingG = (float) v);
        addDrawableChild(gSlider);
        y += rowH;

        // ── B slider ─────────────────────────────────────────────────────────
        bSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.of("Blue"),
                0.0, 2.0, workingB,
                v -> workingB = (float) v);
        addDrawableChild(bSlider);
        y += rowH + 10;

        // ── Reset to Neutral button ───────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.colorgradebd.reset"),
                btn -> resetColor())
                .dimensions(cx - 75, y, 150, 20)
                .build());

        // ── Apply / Cancel buttons ────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                btn -> apply())
                .dimensions(cx - 78, this.height - 26, 74, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(
                ScreenTexts.CANCEL,
                btn -> cancel())
                .dimensions(cx + 4, this.height - 26, 74, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Title
        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 14, 0xFFFFFF);

        // Color preview swatch (4 px outline + filled rectangle)
        int swatchSize = 40;
        int swatchX    = this.width / 2 - swatchSize / 2;
        int swatchY    = 24;
        int r = clamp8(workingR);
        int g = clamp8(workingG);
        int b = clamp8(workingB);
        int color = 0xFF000000 | (r << 16) | (g << 8) | b;
        context.fill(swatchX - 2, swatchY - 2, swatchX + swatchSize + 2, swatchY + swatchSize + 2, 0xFF555555);
        context.fill(swatchX, swatchY, swatchX + swatchSize, swatchY + swatchSize, color);

        // Hex value
        context.drawCenteredTextWithShadow(textRenderer,
                Text.of(String.format("#%02X%02X%02X", r, g, b)),
                this.width / 2, swatchY + swatchSize + 4, 0xCCCCCC);

        super.render(context, mouseX, mouseY, delta);
    }

    private void apply() {
        settings.colorR = workingR;
        settings.colorG = workingG;
        settings.colorB = workingB;
        if (client != null) client.setScreen(parent);
    }

    private void cancel() {
        if (client != null) client.setScreen(parent);
    }

    private void resetColor() {
        workingR = 1.0f;
        workingG = 1.0f;
        workingB = 1.0f;
        // Re-init to reset slider positions
        clearChildren();
        init();
    }

    /** Clamp multiplier (0..2) to 8-bit (0..255) for preview. */
    private int clamp8(float multiplier) {
        return (int) Math.max(0, Math.min(255, multiplier * 128));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { cancel(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
