package com.ahmednotxgamer.colorgradebd.client.gui.widgets;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;

/**
 * A vanilla-compatible slider widget that displays its label and current value.
 * Supports real-time callbacks as the user drags the slider.
 *
 * Label format: "Brightness: 0.50"  (or percentage for 0..1 ranges)
 */
public class LabeledSliderWidget extends SliderWidget {

    private final Text baseLabel;
    private final double min;
    private final double max;
    private final DoubleConsumer onChange;

    /** Construct with explicit range and initial value. */
    public LabeledSliderWidget(int x, int y, int width, int height,
                               Text label,
                               double min, double max, double initialValue,
                               DoubleConsumer onChange) {
        super(x, y, width, height,
                label, // message (overwritten in updateMessage)
                clampNormalize(initialValue, min, max));
        this.baseLabel = label;
        this.min       = min;
        this.max       = max;
        this.onChange  = onChange;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        double realValue = denormalize(this.value);
        String formatted = formatValue(realValue);
        this.setMessage(Text.of(baseLabel.getString() + ": " + formatted));
    }

    @Override
    protected void applyValue() {
        if (onChange != null) {
            onChange.accept(denormalize(this.value));
        }
    }

    /** Externally set the slider value (e.g., on reset). Accepts real-world value. */
    public void setValue(double realValue) {
        this.value = clampNormalize(realValue, min, max);
        updateMessage();
    }

    /** Get the current real-world value. */
    public double getRealValue() {
        return denormalize(this.value);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private double denormalize(double normalized) {
        return min + normalized * (max - min);
    }

    private static double clampNormalize(double value, double min, double max) {
        if (max == min) return 0.0;
        double normalized = (value - min) / (max - min);
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    /**
     * Format the real value for display.
     * - For 0..1 ranges: show as percentage  (e.g. "75%")
     * - For -1..1 ranges: show as +/- percentage (e.g. "+50%", "-25%")
     * - For hue (-180..180): show degrees (e.g. "+45°")
     * - For gamma (0.5..2.0): show 2 decimal places
     * - For color channels (0..2): show 2 decimal places
     */
    private String formatValue(double v) {
        if (min == -180.0 && max == 180.0) {
            return String.format("%+.0f°", v);
        }
        if (min == 0.5 && max == 2.0) {
            return String.format("%.2f", v);
        }
        if (min == 0.0 && max == 2.0) {
            return String.format("%.2f", v);
        }
        if (min == -1.0 && max == 1.0) {
            return String.format("%+.0f%%", v * 100.0);
        }
        if (min == 0.0 && max == 1.0) {
            return String.format("%.0f%%", v * 100.0);
        }
        return String.format("%.2f", v);
    }
}
