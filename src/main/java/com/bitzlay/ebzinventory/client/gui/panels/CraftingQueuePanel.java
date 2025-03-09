package com.bitzlay.ebzinventory.client.gui.panels;

import com.bitzlay.ebzinventory.client.gui.components.QueueItemComponent;
import com.bitzlay.ebzinventory.crafting.CraftingQueueHandler;
import com.bitzlay.ebzinventory.crafting.CraftingQueueItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Panel showing the current crafting queue
 */
public class CraftingQueuePanel extends BasePanel {
    private static final int QUEUE_SPACING = 4;
    private static final int QUEUE_CARD_HEIGHT = 30;

    private final UUID playerUUID;
    private final Consumer<Integer> onItemCancel;
    private final Runnable onClearQueue;

    private int queuePage = 0;
    private static final int ITEMS_PER_PAGE = 5;

    public CraftingQueuePanel(Screen parentScreen, int x, int y, int width, int height,
                              UUID playerUUID, Consumer<Integer> onItemCancel, Runnable onClearQueue) {
        super(parentScreen, x, y, width, height, "Cola de Crafteo");
        this.playerUUID = playerUUID;
        this.onItemCancel = onItemCancel;
        this.onClearQueue = onClearQueue;
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        List<CraftingQueueItem> queue = CraftingQueueHandler.getPlayerQueue(playerUUID);
        if (queue.isEmpty()) {
            guiGraphics.drawString(parentScreen.getMinecraft().font,
                    "No hay elementos en la cola", x + 10, y + 30, 0xAAAAAA);
            return;
        }

        // Clear existing widgets
        clearWidgets();

        // Add cancel all button
        renderCancelAllButton(guiGraphics, mouseX, mouseY);

        // Pagination setup
        int totalPages = (queue.size() - 1) / ITEMS_PER_PAGE + 1;
        int startIndex = queuePage * ITEMS_PER_PAGE;

        // Ensure valid page
        if (startIndex >= queue.size()) {
            queuePage = 0;
            startIndex = 0;
        }

        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, queue.size());

        // Render queue items
        int itemY = y + 25;
        for (int i = startIndex; i < endIndex; i++) {
            if (i >= queue.size()) break;

            CraftingQueueItem item = queue.get(i);
            QueueItemComponent queueItemComponent = new QueueItemComponent(
                    x + 5, itemY, width - 10, item, i, onItemCancel, parentScreen);
            addWidget(queueItemComponent);

            itemY += QUEUE_CARD_HEIGHT + QUEUE_SPACING;
        }

        // Add navigation buttons
        addNavigationButtons(totalPages);
    }

    private void renderCancelAllButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int cancelAllX = x + width - 25;
        int cancelAllY = y + 5;
        boolean isHovered = mouseX >= cancelAllX && mouseX < cancelAllX + 20 &&
                mouseY >= cancelAllY && mouseY < cancelAllY + 20;

        if (isHovered) {
            guiGraphics.fill(cancelAllX, cancelAllY, cancelAllX + 20, cancelAllY + 20, 0xFF666666);
        }

        guiGraphics.drawString(parentScreen.getMinecraft().font, "×", cancelAllX + 7, cancelAllY + 6, 0xFFFFFF);
    }

    private void addNavigationButtons(int totalPages) {
        if (totalPages <= 1) return;

        int navButtonY = y + height - 25;
        int navButtonWidth = 50;

        // Previous page button
        if (queuePage > 0) {
            Button prevButton = Button.builder(Component.literal("<"), b -> {
                queuePage--;
                clearWidgets();
            }).pos(x + 10, navButtonY).size(navButtonWidth, 20).build();
            addWidget(prevButton);
        }

        // Home button (only if not on first page)
        if (queuePage > 0) {
            Button homeButton = Button.builder(Component.literal("Inicio"), b -> {
                queuePage = 0;
                clearWidgets();
            }).pos(x + (width - navButtonWidth)/2, navButtonY).size(navButtonWidth, 20).build();
            addWidget(homeButton);
        }

        // Next page button
        if (queuePage < totalPages - 1) {
            Button nextButton = Button.builder(Component.literal(">"), b -> {
                queuePage++;
                clearWidgets();
            }).pos(x + width - navButtonWidth - 10, navButtonY).size(navButtonWidth, 20).build();
            addWidget(nextButton);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if cancel all button was clicked
        int cancelAllX = x + width - 25;
        int cancelAllY = y + 5;

        if (mouseX >= cancelAllX && mouseX < cancelAllX + 20 &&
                mouseY >= cancelAllY && mouseY < cancelAllY + 20) {
            if (onClearQueue != null) {
                onClearQueue.run();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void setQueuePage(int page) {
        this.queuePage = page;
    }
}