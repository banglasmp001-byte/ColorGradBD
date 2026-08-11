package com.ahmednotxgamer.colorgradebd.client.gui.screens;

import com.ahmednotxgamer.colorgradebd.client.render.ColorGradingRenderer;
import com.ahmednotxgamer.colorgradebd.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ColorGradeScreen extends Screen implements WidgetHolder {

    private static final int TAB_GAME    = 0;
    private static final int TAB_ELEMENT = 1;

    private int activeTab = TAB_GAME;
    private final Screen parent;

    private GameColorGradingScreen gameGradingPanel;
    private ElementGradingScreen   elementGradingPanel;

    public ColorGradeScreen(Screen parent) {
        super(Text.translatable("screen.colorgradebd.title"));
        this.parent = parent;
    }

    @Override
    public void addWidget(ClickableWidget widget) {
        addDrawableChild(widget);
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int topY = 8;
        int tabW = 140;
        int tabH = 20;
        int gap  = 4;

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("tab.colorgradebd.game_grading"),
                btn -> switchTab(TAB_GAME))
                .dimensions(centerX - tabW - gap / 2, topY, tabW, tabH)
                .build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("tab.colorgradebd.element_grading"),
                btn -> switchTab(TAB_ELEMENT))
                .dimensions(centerX + gap / 2, topY, tabW, tabH)
                .build());

        addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE, btn -> saveAndClose())
                .dimensions(centerX - 75, this.height - 26, 150, 20)
                .build());

        int panelTop = topY + tabH + 6;
        int panelH   = this.height - panelTop - 32;

        gameGradingPanel    = new GameColorGradingScreen(this, panelTop, panelH);
        elementGradingPanel = new ElementGradingScreen(this, panelTop, panelH);

        gameGradingPanel.init(client, this.width, this.height);
        elementGradingPanel.init(client, this.width, this.height);

        showActiveTab();
    }

    private void switchTab(int tab) {
        activeTab = tab;
        clearChildren();
        init();
    }

    private void showActiveTab() {
        if (activeTab == TAB_GAME) {
            gameGradingPanel.addWidgetsTo(this);
        } else {
            elementGradingPanel.addWidgetsTo(this);
        }
    }

    private void saveAndClose() {
        ConfigManager.getInstance().save();
        ColorGradingRenderer.getInstance().markDirty();
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("screen.colorgradebd.title"),
                this.width / 2, 35, 0xFFFFFF);

        int centerX = this.width / 2;
        int lineY   = 8 + 20 + 2;
        int tabW    = 140;
        int gap     = 4;
        if (activeTab == TAB_GAME) {
            context.fill(centerX - tabW - gap/2, lineY, centerX - gap/2, lineY + 2, 0xFFFFFFFF);
        } else {
            context.fill(centerX + gap/2, lineY, centerX + tabW + gap/2, lineY + 2, 0xFFFFFFFF);
        }

        if (activeTab == TAB_ELEMENT) {
            elementGradingPanel.renderContent(context, mouseX, mouseY, delta);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { saveAndClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
