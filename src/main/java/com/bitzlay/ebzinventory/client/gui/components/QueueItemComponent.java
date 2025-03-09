package com.bitzlay.ebzinventory.client.gui.components;

import com.bitzlay.ebzinventory.crafting.CraftingQueueItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * UI component representing an item in the crafting queue
 */
public class QueueItemComponent extends AbstractWidget {
    private static final int CARD_HEIGHT = 30;

    private final CraftingQueueItem queueItem;
    private final int index;
    private final Consumer<Integer> onCancel;
    private final Screen parentScreen;

    public QueueItemComponent(int x, int y, int width, CraftingQueueItem queueItem,
                              int index, Consumer<Integer> onCancel, Screen parentScreen) {
        super(x, y, width, CARD_HEIGHT, Component.empty());
        this.queueItem = queueItem;
        this.index = index;
        this.onCancel = onCancel;
        this.parentScreen = parentScreen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render background
        renderBackground(guiGraphics);

        // Render item icon
        guiGraphics.renderItem(queueItem.getResult(), getX() + 5, getY());

        // Render recipe name
        String itemName = queueItem.getRecipe().getDisplayName();
        int maxTextWidth = width - 50;
        String truncatedName = parentScreen.getMinecraft().font.plainSubstrByWidth(itemName, maxTextWidth);
        guiGraphics.drawString(parentScreen.getMinecraft().font, truncatedName, getX() + 25, getY() + 3, 0xFFFFFF);

        // Render progress bar
        renderProgressBar(guiGraphics);

        // Render percentage
        float progress = queueItem.getProgress() * 100;
        String percentage = String.format("%.0f%%", progress);
        guiGraphics.drawString(parentScreen.getMinecraft().font, percentage,
                getX() + width - 30, getY() + CARD_HEIGHT - 12, 0xAAAAAA);

        // Render cancel button
        renderCancelButton(guiGraphics, mouseX, mouseY);
    }

    private void renderBackground(GuiGraphics guiGraphics) {
        // Main background
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + CARD_HEIGHT, 0xFF333333);

        // Border
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + 1, 0xFF555555);
        guiGraphics.fill(getX(), getY() + CARD_HEIGHT - 1, getX() + width, getY() + CARD_HEIGHT, 0xFF555555);
        guiGraphics.fill(getX(), getY(), getX() + 1, getY() + CARD_HEIGHT, 0xFF555555);
        guiGraphics.fill(getX() + width - 1, getY(), getX() + width, getY() + CARD_HEIGHT, 0xFF555555);
    }

    private void renderProgressBar(GuiGraphics guiGraphics) {
        int barWidth = width - 40;
        int barHeight = 4;
        int barY = getY() + CARD_HEIGHT - 7;

        // Background
        guiGraphics.fill(getX() + 5, barY, getX() + 5 + barWidth, barY + barHeight, 0xFF666666);

        // Progress
        float progress = queueItem.getProgress();
        int progressWidth = (int)(barWidth * progress);
        int progressColor = queueItem.isPaused() ? 0xFFFFAA00 : 0xFF00FF00;
        guiGraphics.fill(getX() + 5, barY, getX() + 5 + progressWidth, barY + barHeight, progressColor);
    }

    private void renderCancelButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int cancelX = getX() + width - 15;
        int cancelY = getY() + 5;

        boolean isHovered = mouseX >= cancelX && mouseX < cancelX + 10 &&
                mouseY >= cancelY && mouseY < cancelY + 10;

        if (isHovered) {
            guiGraphics.fill(cancelX, cancelY, cancelX + 10, cancelY + 10, 0xFF666666);
        }

        guiGraphics.drawString(parentScreen.getMinecraft().font, "×", cancelX + 2, cancelY, 0xFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                "Crafting queue item " + queueItem.getRecipe().getDisplayName());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if cancel button was clicked
        int cancelX = getX() + width - 15;
        int cancelY = getY() + 5;

        if (mouseX >= cancelX && mouseX < cancelX + 10 &&
                mouseY >= cancelY && mouseY < cancelY + 10) {
            if (onCancel != null) {
                onCancel.accept(index);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public CraftingQueueItem getQueueItem() {
        return queueItem;
    }

    public int getIndex() {
        return index;
    }
}