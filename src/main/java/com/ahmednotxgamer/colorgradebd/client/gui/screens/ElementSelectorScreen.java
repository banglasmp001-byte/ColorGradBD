package com.ahmednotxgamer.colorgradebd.client.gui.screens;

import com.ahmednotxgamer.colorgradebd.element.ElementEntry;
import com.ahmednotxgamer.colorgradebd.element.ElementManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

/**
 * Searchable, scrollable element/block picker.
 *
 * Dynamically queries Minecraft's block registry at runtime.
 * No hardcoded block lists.
 *
 * The player types in the search box, results update immediately.
 * Scrollable list handles hundreds of results.
 * Select an entry and click [ ADD ] to return the registry key to the caller.
 */
public class ElementSelectorScreen extends Screen {

    private static final int ROW_H   = 18;
    private static final int MAX_VIS = 12;

    private final Screen parent;
    private final Consumer<String> onSelect;

    private TextFieldWidget searchField;
    private List<ElementEntry> filteredEntries;
    private int scrollOffset = 0;
    private String selectedKey = null;

    // Layout
    private int listTop;
    private int listW;
    private int listX;

    public ElementSelectorScreen(Screen parent, Consumer<String> onSelect) {
        super(Text.translatable("screen.colorgradebd.select_element"));
        this.parent   = parent;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        super.init();

        int cx = this.width / 2;
        listW  = Math.min(320, this.width - 60);
        listX  = cx - listW / 2;

        // ── Search box ───────────────────────────────────────────────────────
        searchField = new TextFieldWidget(textRenderer,
                listX, 36, listW, 20,
                Text.translatable("text.colorgradebd.search"));
        searchField.setMaxLength(64);
        searchField.setPlaceholderText(Text.translatable("text.colorgradebd.search_hint"));
        searchField.setChangedListener(this::onSearchChanged);
        addSelectableChild(searchField);
        setInitialFocus(searchField);

        listTop = 62;

        // ── ADD button ───────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("button.colorgradebd.add"),
                btn -> addSelected())
                .dimensions(cx - 78, this.height - 26, 74, 20)
                .build());

        // ── Cancel button ────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                ScreenTexts.CANCEL,
                btn -> close())
                .dimensions(cx + 4, this.height - 26, 74, 20)
                .build());

        // Initial full list
        filteredEntries = ElementManager.getInstance().getAllElements();
    }

    private void onSearchChanged(String query) {
        filteredEntries = ElementManager.getInstance().search(query);
        scrollOffset    = 0;
        selectedKey     = null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Title
        context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("screen.colorgradebd.select_element"),
                this.width / 2, 14, 0xFFFFFF);

        // Subtitle: result count
        String countText = filteredEntries.size() + " " +
                (filteredEntries.size() == 1 ? "element" : "elements");
        context.drawCenteredTextWithShadow(textRenderer,
                Text.of("§7" + countText),
                this.width / 2, 26, 0xFFFFFF);

        // Search field
        searchField.render(context, mouseX, mouseY, delta);

        // List background
        int listH = MAX_VIS * ROW_H;
        context.fill(listX - 2, listTop - 2, listX + listW + 2, listTop + listH + 2, 0xFF000000);
        context.fill(listX - 1, listTop - 1, listX + listW + 1, listTop + listH + 1, 0xFF222222);

        // Scrollable list rows
        int visible = Math.min(MAX_VIS, filteredEntries.size() - scrollOffset);
        for (int i = 0; i < visible; i++) {
            int idx   = i + scrollOffset;
            if (idx >= filteredEntries.size()) break;
            ElementEntry entry = filteredEntries.get(idx);
            int rowY = listTop + i * ROW_H;
            boolean isSelected = entry.getRegistryKey().equals(selectedKey);

            // Highlight selected row
            if (isSelected) {
                context.fill(listX, rowY, listX + listW, rowY + ROW_H - 1, 0xFF3344AA);
            } else if (mouseX >= listX && mouseX < listX + listW &&
                       mouseY >= rowY && mouseY < rowY + ROW_H) {
                context.fill(listX, rowY, listX + listW, rowY + ROW_H - 1, 0xFF444444);
            }

            // Category tag color
            String catPrefix = switch (entry.getCategory()) {
                case ENVIRONMENT  -> "§b";  // aqua
                case VANILLA_BLOCK -> "";
                case MODDED_BLOCK  -> "§6"; // gold
            };

            // Namespace prefix for modded (dim)
            String label = catPrefix + entry.getDisplayName();
            if (entry.getCategory() == ElementEntry.Category.MODDED_BLOCK &&
                    entry.getRegistryKey().contains(":")) {
                String ns = entry.getRegistryKey().split(":")[0];
                label += " §8(" + ns + ")";
            }

            context.drawTextWithShadow(textRenderer, Text.of(label),
                    listX + 3, rowY + (ROW_H - 8) / 2, 0xFFFFFF);
        }

        // Scrollbar
        if (filteredEntries.size() > MAX_VIS) {
            int trackH    = MAX_VIS * ROW_H;
            int thumbH    = Math.max(10, trackH * MAX_VIS / filteredEntries.size());
            int thumbY    = listTop + scrollOffset * (trackH - thumbH) / Math.max(1, filteredEntries.size() - MAX_VIS);
            int scrollX   = listX + listW + 3;
            context.fill(scrollX, listTop, scrollX + 4, listTop + trackH, 0xFF333333);
            context.fill(scrollX, thumbY, scrollX + 4, thumbY + thumbH, 0xFF888888);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Row click detection
        if (mouseX >= listX && mouseX < listX + listW) {
            int relY = (int) mouseY - listTop;
            if (relY >= 0 && relY < MAX_VIS * ROW_H) {
                int idx = scrollOffset + relY / ROW_H;
                if (idx < filteredEntries.size()) {
                    selectedKey = filteredEntries.get(idx).getRegistryKey();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, filteredEntries.size() - MAX_VIS);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            close();
            return true;
        }
        if (keyCode == 257 || keyCode == 335) { // Enter / numpad enter
            addSelected();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers) || searchField.keyPressed(keyCode, scanCode, modifiers);
    }

    private void addSelected() {
        if (selectedKey != null && onSelect != null) {
            onSelect.accept(selectedKey);
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
