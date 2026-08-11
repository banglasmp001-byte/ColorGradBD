package com.ahmednotxgamer.colorgradebd.client.gui.screens;

import net.minecraft.client.gui.widget.ClickableWidget;

/**
 * Interface implemented by ColorGradeScreen so inner panels
 * can add widgets through a public method (bypassing protected access).
 */
public interface WidgetHolder {
    void addWidget(ClickableWidget widget);
}
