package com.ahmednotxgamer.colorgradebd.client.gui.screens;

import com.ahmednotxgamer.colorgradebd.client.gui.widgets.LabeledSliderWidget;
import com.ahmednotxgamer.colorgradebd.client.render.ColorGradingRenderer;
import com.ahmednotxgamer.colorgradebd.config.ConfigManager;
import com.ahmednotxgamer.colorgradebd.config.GlobalColorSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The Game Color Grading tab content.
 * Renders all global grading sliders with vanilla-style layout.
 * Sliders update settings in real time as they are dragged.
 * Scroll support for smaller windows.
 */
public class GameColorGradingScreen {

    private final Screen parent;
    private final int panelTop;
    private final int panelHeight;

    private final List<net.minecraft.client.gui.widget.ClickableWidget> widgets = new ArrayList<>();

    // Scroll
    private int scrollOffset = 0;
    private static final int ROW_H = 28;

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
    private LabeledSliderWidget gammaSlider;
    private LabeledSliderWidget temperatureSlider;
    private LabeledSliderWidget vignetteSlider;

    private ButtonWidget enabledButton;

    private int screenWidth;

    public GameColorGradingScreen(Screen parent, int panelTop, int panelHeight) {
        this.parent      = parent;
        this.panelTop    = panelTop;
        this.panelHeight = panelHeight;
    }

    public void init(MinecraftClient client, int width, int height) {
        widgets.clear();
        screenWidth = width;

        GlobalColorSettings s = ConfigManager.getInstance().getGlobal();

        int cx      = width / 2;
        int sliderW = Math.min(260, width - 100);
        int labelX  = cx - sliderW / 2;
        int sliderX = cx - sliderW / 2;

        int y = panelTop + 4;

        // ── Enable/Disable button ────────────────────────────────────────────
        enabledButton = ButtonWidget.builder(
                enabledText(s.enabled),
                btn -> {
                    GlobalColorSettings gs = ConfigManager.getInstance().getGlobal();
                    gs.enabled = !gs.enabled;
                    btn.setMessage(enabledText(gs.enabled));
                    ColorGradingRenderer.getInstance().markDirty();
                })
                .dimensions(cx - 75, y, 150, 20)
                .build();
        widgets.add(enabledButton);
        y += 26;

        // ── Sliders ──────────────────────────────────────────────────────────
        brightnessSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.brightness"),
                -1.0, 1.0, s.brightness,
                v -> ConfigManager.getInstance().getGlobal().brightness = (float) v);
        widgets.add(brightnessSlider);
        y += ROW_H;

        contrastSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.contrast"),
                -1.0, 1.0, s.contrast,
                v -> ConfigManager.getInstance().getGlobal().contrast = (float) v);
        widgets.add(contrastSlider);
        y += ROW_H;

        saturationSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.saturation"),
                -1.0, 1.0, s.saturation,
                v -> ConfigManager.getInstance().getGlobal().saturation = (float) v);
        widgets.add(saturationSlider);
        y += ROW_H;

        hueSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.hue"),
                -180.0, 180.0, s.hue,
                v -> ConfigManager.getInstance().getGlobal().hue = (float) v);
        widgets.add(hueSlider);
        y += ROW_H;

        sharpnessSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.sharpness"),
                0.0, 1.0, s.sharpness,
                v -> ConfigManager.getInstance().getGlobal().sharpness = (float) v);
        widgets.add(sharpnessSlider);
        y += ROW_H;

        colorRSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.color_r"),
                0.0, 2.0, s.colorR,
                v -> ConfigManager.getInstance().getGlobal().colorR = (float) v);
        widgets.add(colorRSlider);
        y += ROW_H;

        colorGSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.color_g"),
                0.0, 2.0, s.colorG,
                v -> ConfigManager.getInstance().getGlobal().colorG = (float) v);
        widgets.add(colorGSlider);
        y += ROW_H;

        colorBSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.color_b"),
                0.0, 2.0, s.colorB,
                v -> ConfigManager.getInstance().getGlobal().colorB = (float) v);
        widgets.add(colorBSlider);
        y += ROW_H;

        intensitySlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.intensity"),
                0.0, 1.0, s.intensity,
                v -> ConfigManager.getInstance().getGlobal().intensity = (float) v);
        widgets.add(intensitySlider);
        y += ROW_H;

        gammaSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.gamma"),
                0.5, 2.0, s.gamma,
                v -> ConfigManager.getInstance().getGlobal().gamma = (float) v);
        widgets.add(gammaSlider);
        y += ROW_H;

        temperatureSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.temperature"),
                -1.0, 1.0, s.temperature,
                v -> ConfigManager.getInstance().getGlobal().temperature = (float) v);
        widgets.add(temperatureSlider);
        y += ROW_H;

        vignetteSlider = new LabeledSliderWidget(sliderX, y, sliderW, 20,
                Text.translatable("slider.colorgradebd.vignette"),
                0.0, 1.0, s.vignette,
                v -> ConfigManager.getInstance().getGlobal().vignette = (float) v);
        widgets.add(vignetteSlider);
        y += ROW_H;

        // ── Reset to Default button ──────────────────────────────────────────
        ButtonWidget resetBtn = ButtonWidget.builder(
                Text.translatable("button.colorgradebd.reset_all"),
                btn -> resetAll())
                .dimensions(cx - 75, y + 4, 150, 20)
                .build();
        widgets.add(resetBtn);
    }

    /** Add all widgets from this panel to the parent screen. */
    public void addWidgetsTo(WidgetHolder screen) {
        for (var w : widgets) {
            screen.addWidget(w);
        }
    }

    public void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // Nothing extra to render for this tab; widgets handle themselves
    }

    private void resetAll() {
        ConfigManager.getInstance().getGlobal().resetToDefault();
        ColorGradingRenderer.getInstance().markDirty();
        // Re-sync slider visual values
        GlobalColorSettings s = ConfigManager.getInstance().getGlobal();
        brightnessSlider.setValue(s.brightness);
        contrastSlider.setValue(s.contrast);
        saturationSlider.setValue(s.saturation);
        hueSlider.setValue(s.hue);
        sharpnessSlider.setValue(s.sharpness);
        colorRSlider.setValue(s.colorR);
        colorGSlider.setValue(s.colorG);
        colorBSlider.setValue(s.colorB);
        intensitySlider.setValue(s.intensity);
        gammaSlider.setValue(s.gamma);
        temperatureSlider.setValue(s.temperature);
        vignetteSlider.setValue(s.vignette);
        if (enabledButton != null) {
            enabledButton.setMessage(enabledText(s.enabled));
        }
    }

    private Text enabledText(boolean enabled) {
        return enabled
                ? Text.translatable("button.colorgradebd.enabled")
                : Text.translatable("button.colorgradebd.disabled");
    }
}
