package com.ahmednotxgamer.colorgradebd.client.gui.screens;

import com.ahmednotxgamer.colorgradebd.client.gui.widgets.LabeledSliderWidget;
import com.ahmednotxgamer.colorgradebd.config.ConfigManager;
import com.ahmednotxgamer.colorgradebd.config.ElementColorSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Full per-element grading editor.
 * Provides all color controls plus color picker access and depth effect slider.
 * Scrollable for smaller windows.
 */
public class ElementEditScreen extends Screen {

    private final Screen parent;
    private final ElementColorSettings settings;

    // Sliders
    private LabeledSliderWidget brightnessSlider;
    private LabeledSliderWidget contrastSlider;
    private LabeledSliderWidget saturationSlider;
    private LabeledSliderWidget hueSlider;
    private LabeledSliderWidget sharpnessSlider;
    private LabeledSliderWidget colorRSlider;
    private LabeledSliderWidget colorGSlider;
    private LabeledSliderWidget colorBSlider;
    private LabeledSliderWidget intensitySlider;
    private LabeledSliderWidget depthSlider;

    private ButtonWidget enabledButton;

    // Scroll state
    private int scrollOffset = 0;
    private int contentHeight;
    private static final int ROW_H = 26;

    public ElementEditScreen(Screen parent, ElementColorSettings settings) {
        super(Text.of("Edit: " + (settings.displayName.isEmpty()
                ? settings.registryKey : settings.displayName)));
        this.parent   = parent;
        this.settings = settings;
    }

    @Override
    protected void init() {
        super.init();
        int cx      = this.width / 2;
        int sliderW = Math.min(260, this.width - 100);
        int sliderX = cx - sliderW / 2;
        int topY    = 50; // Start below title
        int y       = topY - scrollOffset;

        // ── Enable/Disable ───────────────────────────────────────────────────
        enabledButton = ButtonWidget.builder(
                enabledText(settings.enabled),
                btn -> {
                    settings.enabled = !settings.enabled;
                    btn.setMessage(enabledText(settings.enabled));
                })
                .dimensions(cx - 75, y, 150, 20)
                .build();
        addDrawableChild(enabledButton);
        y += ROW_H;

        // ── Color Picker button ──────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.colorgradebd.color_picker"),
                btn -> openColorPicker())
                .dimensions(cx - 75, y, 150, 20)
                .build());
        y += ROW_H;

        // ── Sliders ──────────────────────────────────────────────────────────
        brightnessSlider = addSlider(new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.brightness"),
                -1.0, 1.0, settings.brightness,
                v -> settings.brightness = (float) v));
        y += ROW_H;

        contrastSlider = addSlider(new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.contrast"),
                -1.0, 1.0, settings.contrast,
                v -> settings.contrast = (float) v));
        y += ROW_H;

        saturationSlider = addSlider(new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.saturation"),
                -1.0, 1.0, settings.saturation,
                v -> settings.saturation = (float) v));
        y += ROW_H;

        hueSlider = addSlider(new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.hue"),
                -180.0, 180.0, settings.hue,
                v -> settings.hue = (float) v));
        y += ROW_H;

        sharpnessSlider = addSlider(new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.sharpness"),
                0.0, 1.0, settings.sharpness,
                v -> settings.sharpness = (float) v));
        y += ROW_H;

        colorRSlider = addSlider(new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.color_r"),
                0.0, 2.0, settings.colorR,
                v -> settings.colorR = (float) v));
        y += ROW_H;

        colorGSlider = addSlider(new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.color_g"),
                0.0, 2.0, settings.colorG,
                v -> settings.colorG = (float) v));
        y += ROW_H;

        colorBSlider = addSlider(new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.color_b"),
                0.0, 2.0, settings.colorB,
                v -> settings.colorB = (float) v));
        y += ROW_H;

        intensitySlider = addSlider(new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.intensity"),
                0.0, 1.0, settings.intensity,
                v -> settings.intensity = (float) v));
        y += ROW_H;

        depthSlider = addSlider(new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.depth_effect"),
                0.0, 1.0, settings.depthEffect,
                v -> settings.depthEffect = (float) v));
        y += ROW_H;

        contentHeight = y + ROW_H + 40;

        // ── Reset button ─────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.colorgradebd.reset_element"),
                btn -> resetSettings())
                .dimensions(cx - 155, this.height - 26, 100, 20)
                .build());

        // ── Save/Done button ─────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                btn -> saveAndClose())
                .dimensions(cx - 50, this.height - 26, 100, 20)
                .build());

        // ── Cancel button ────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                ScreenTexts.CANCEL,
                btn -> cancelAndClose())
                .dimensions(cx + 55, this.height - 26, 100, 20)
                .build());
    }

    private <T extends net.minecraft.client.gui.widget.ClickableWidget> T addSlider(T slider) {
        addDrawableChild(slider);
        return slider;
    }

    private void openColorPicker() {
        if (client != null) {
            client.setScreen(new ColorPickerScreen(this, settings));
        }
    }

    private void resetSettings() {
        settings.resetToDefault();
        // Re-init to sync slider positions
        clearChildren();
        init();
    }

    private void saveAndClose() {
        ConfigManager.getInstance().putElement(settings);
        ConfigManager.getInstance().save();
        if (client != null) client.setScreen(parent);
    }

    private void cancelAndClose() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Screen title
        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 14, 0xFFFFFF);

        // Registry key subtitle
        context.drawCenteredTextWithShadow(textRenderer,
                Text.of("§7" + settings.registryKey),
                this.width / 2, 26, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        int maxScroll = Math.max(0, contentHeight - this.height + 40);
        scrollOffset  = (int) Math.max(0, Math.min(maxScroll, scrollOffset - vAmount * 10));
        // Re-init to reposition widgets at new scroll offset
        clearChildren();
        init();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            cancelAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private Text enabledText(boolean enabled) {
        return enabled
                ? Text.translatable("button.colorgradebd.enabled")
                : Text.translatable("button.colorgradebd.disabled");
    }
}
