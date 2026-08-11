package com.ahmednotxgamer.colorgradebd.client.gui.screens;

import com.ahmednotxgamer.colorgradebd.config.ConfigManager;
import com.ahmednotxgamer.colorgradebd.config.ElementColorSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The Element Grading tab content.
 *
 * Shows the list of configured elements as a scrollable selector.
 * Provides buttons:
 *   [ ADD ELEMENT ]  [ EDIT ]  [ DELETE ]  [ RESET ]  [ ENABLE/DISABLE ]
 *
 * Selected element's settings are shown/edited via ElementEditScreen (child screen).
 */
public class ElementGradingScreen {

    private final Screen parent;
    private final int panelTop;
    private final int panelHeight;

    private final List<net.minecraft.client.gui.widget.ClickableWidget> widgets = new ArrayList<>();

    private int screenWidth;
    private int screenHeight;
    private MinecraftClient client;

    private String selectedKey = null; // Currently selected element registry key

    public ElementGradingScreen(Screen parent, int panelTop, int panelHeight) {
        this.parent      = parent;
        this.panelTop    = panelTop;
        this.panelHeight = panelHeight;
    }

    public void init(MinecraftClient client, int width, int height) {
        this.client      = client;
        this.screenWidth = width;
        this.screenHeight = height;
        widgets.clear();
        buildWidgets();
    }

    private void buildWidgets() {
        int cx   = screenWidth / 2;
        int btnW = 130;
        int y    = panelTop + 4;

        // ── ADD ELEMENT ──────────────────────────────────────────────────────
        ButtonWidget addBtn = ButtonWidget.builder(
                Text.translatable("button.colorgradebd.add_element"),
                btn -> openElementSelector())
                .dimensions(cx - btnW / 2, y, btnW, 20)
                .build();
        widgets.add(addBtn);
        y += 26;

        // ── Configured element list (simple scrollable) ───────────────────
        List<ElementColorSettings> elements = ConfigManager.getInstance().getConfig().elements;

        if (elements.isEmpty()) {
            // No elements configured yet — show hint text (rendered in renderContent)
        } else {
            int listTop = y;
            int maxVisible = (panelHeight - 80) / 22;
            int shown = Math.min(maxVisible, elements.size());

            for (int i = 0; i < shown; i++) {
                ElementColorSettings elem = elements.get(i);
                final String key = elem.registryKey;
                final String displayName = elem.displayName.isEmpty()
                        ? elem.registryKey : elem.displayName;

                // Row button — clicking selects this element
                ButtonWidget rowBtn = ButtonWidget.builder(
                        Text.of((elem.enabled ? "§a" : "§7") + displayName),
                        btn -> selectElement(key))
                        .dimensions(cx - 160, y + (i * 22), 160, 20)
                        .build();
                widgets.add(rowBtn);

                // EDIT button
                ButtonWidget editBtn = ButtonWidget.builder(
                        Text.translatable("button.colorgradebd.edit"),
                        btn -> openEditScreen(key))
                        .dimensions(cx - 160 + 162, y + (i * 22), 50, 20)
                        .build();
                widgets.add(editBtn);

                // DELETE button
                ButtonWidget delBtn = ButtonWidget.builder(
                        Text.translatable("button.colorgradebd.delete"),
                        btn -> deleteElement(key))
                        .dimensions(cx - 160 + 162 + 52, y + (i * 22), 50, 20)
                        .build();
                widgets.add(delBtn);
            }

            y += shown * 22 + 6;

            // ── RESET selected ───────────────────────────────────────────────
            ButtonWidget resetBtn = ButtonWidget.builder(
                    Text.translatable("button.colorgradebd.reset_element"),
                    btn -> { if (selectedKey != null) resetElement(selectedKey); })
                    .dimensions(cx - 75, y, 150, 20)
                    .build();
            widgets.add(resetBtn);
        }
    }

    /** Open the element selector (search + pick from registry). */
    private void openElementSelector() {
        if (client != null) {
            client.setScreen(new ElementSelectorScreen(parent, key -> {
                // Add element if not already present
                ConfigManager.getInstance().findElement(key).ifPresentOrElse(
                        e -> { /* already exists */ },
                        () -> {
                            String display = formatDisplayName(key);
                            ConfigManager.getInstance().putElement(
                                    new ElementColorSettings(key, display));
                            ConfigManager.getInstance().save();
                        });
                // Re-open the main screen to show updated list
                client.setScreen(parent);
            }));
        }
    }

    private void selectElement(String key) {
        selectedKey = key;
    }

    private void openEditScreen(String key) {
        if (client != null) {
            ElementColorSettings settings = ConfigManager.getInstance()
                    .findElement(key)
                    .orElseGet(() -> {
                        ElementColorSettings es = new ElementColorSettings(key, formatDisplayName(key));
                        ConfigManager.getInstance().putElement(es);
                        return es;
                    });
            client.setScreen(new ElementEditScreen(parent, settings));
        }
    }

    private void deleteElement(String key) {
        ConfigManager.getInstance().removeElement(key);
        ConfigManager.getInstance().save();
        if (selectedKey != null && selectedKey.equals(key)) selectedKey = null;
        rebuild();
    }

    private void resetElement(String key) {
        ConfigManager.getInstance().findElement(key).ifPresent(e -> {
            e.resetToDefault();
            ConfigManager.getInstance().save();
        });
    }

    private void rebuild() {
        widgets.clear();
        buildWidgets();
        // Force the parent to re-add our widgets
        if (parent instanceof ColorGradeScreen cgs) {
            // Re-init triggers addWidgetsTo
        }
    }

    private String formatDisplayName(String registryKey) {
        String path = registryKey.contains(":") ? registryKey.split(":")[1] : registryKey;
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }

    /** Add all widgets from this panel to the parent screen. */
    public void addWidgetsTo(WidgetHolder screen) {
        for (var w : widgets) {
            screen.addWidget(w);
        }
    }

    public void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        List<ElementColorSettings> elements = ConfigManager.getInstance().getConfig().elements;
        if (elements.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    MinecraftClient.getInstance().textRenderer,
                    Text.translatable("text.colorgradebd.no_elements"),
                    screenWidth / 2, panelTop + 30, 0xAAAAAA);
        }
    }
}
