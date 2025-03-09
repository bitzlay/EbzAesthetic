package com.bitzlay.ebzinventory.client.renderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

/**
 * Handles rendering of inventory slots with Rust-style appearance
 */
public class InventorySlotRenderer {
    private final int slotSize;
    private final int leftPos;
    private final int topPos;

    public InventorySlotRenderer(int slotSize, int leftPos, int topPos) {
        this.slotSize = slotSize;
        this.leftPos = leftPos;
        this.topPos = topPos;
    }

    /**
     * Renders the background for a slot group (inventory, hotbar, armor)
     */
    public void renderSlotGroupBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Background
        guiGraphics.fill(x - 5, y - 5, x + width + 5, y + height + 5, 0xCC000000);

        // Borders
        guiGraphics.fill(x - 5, y - 5, x + width + 5, y - 4, 0xFF373737);
        guiGraphics.fill(x - 5, y + height + 4, x + width + 5, y + height + 5, 0xFF373737);
        guiGraphics.fill(x - 5, y - 5, x - 4, y + height + 5, 0xFF373737);
        guiGraphics.fill(x + width + 4, y - 5, x + width + 5, y + height + 5, 0xFF373737);
    }

    /**
     * Renders individual slot backgrounds
     */
    public void renderSlotBackground(GuiGraphics guiGraphics, Slot slot, boolean isArmorSlot) {
        if (slot.x < 0 || slot.y < 0) return;

        int offsetToCenter = (slotSize - 16) / 2;
        int actualX = leftPos + slot.x - offsetToCenter;
        int actualY = topPos + slot.y - offsetToCenter;

        // Main slot background
        guiGraphics.fill(actualX, actualY,
                actualX + slotSize, actualY + slotSize,
                0xFF1D1D1D);

        // Standard borders
        guiGraphics.fill(actualX, actualY, actualX + slotSize, actualY + 1, 0xFF373737);
        guiGraphics.fill(actualX, actualY + slotSize - 1, actualX + slotSize, actualY + slotSize, 0xFF373737);
        guiGraphics.fill(actualX, actualY, actualX + 1, actualY + slotSize, 0xFF373737);
        guiGraphics.fill(actualX + slotSize - 1, actualY, actualX + slotSize, actualY + slotSize, 0xFF373737);

        // Special borders for armor slots
        if (isArmorSlot) {
            guiGraphics.fill(actualX - 1, actualY - 1, actualX + slotSize + 1, actualY, 0xFF373737);
            guiGraphics.fill(actualX - 1, actualY + slotSize, actualX + slotSize + 1, actualY + slotSize + 1, 0xFF373737);
            guiGraphics.fill(actualX - 1, actualY - 1, actualX, actualY + slotSize + 1, 0xFF373737);
            guiGraphics.fill(actualX + slotSize, actualY - 1, actualX + slotSize + 1, actualY + slotSize + 1, 0xFF373737);
        }
    }

    /**
     * Sets the position of a slot using reflection
     */
    public void setSlotPosition(Slot slot, int x, int y) {
        try {
            java.lang.reflect.Field xField = net.minecraft.world.inventory.Slot.class.getDeclaredField("x");
            java.lang.reflect.Field yField = net.minecraft.world.inventory.Slot.class.getDeclaredField("y");

            xField.setAccessible(true);
            yField.setAccessible(true);

            int offsetToCenter = (slotSize - 16) / 2;
            xField.set(slot, x + offsetToCenter);
            yField.set(slot, y + offsetToCenter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}