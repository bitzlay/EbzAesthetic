package com.bitzlay.ebzinventory.client.renderer;

import com.bitzlay.ebzinventory.EbzInventory;
import com.bitzlay.ebzinventory.client.gui.screen.RustStyleInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EbzInventory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HotbarRenderer {
    private static final int SLOT_SIZE = 32;
    private static final int SLOT_SPACING = 4;
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 12;
    private static final int BAR_SPACING = 4;

    public static final IGuiOverlay CUSTOM_HOTBAR = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft minecraft = Minecraft.getInstance();
        // No renderizar si el inventario está abierto
        if (minecraft.screen instanceof RustStyleInventoryScreen) return;

        Player player = minecraft.player;
        if (player == null) return;

        int centerX = screenWidth / 2;
        int startX = centerX - ((9 * (SLOT_SIZE + SLOT_SPACING)) / 2);
        int y = screenHeight - SLOT_SIZE - 8;

        // Renderizar stats a la izquierda de la hotbar
        renderPlayerStats(guiGraphics, startX - BAR_WIDTH - 20, y - (BAR_HEIGHT + BAR_SPACING) * 2, player, minecraft);

        // Fondo de la hotbar
        renderHotbarBackground(guiGraphics, startX - 5, y - 5,
                9 * (SLOT_SIZE + SLOT_SPACING) + 10, SLOT_SIZE + 10);

        // Renderizar slots
        for (int slot = 0; slot < 9; slot++) {
            int x = startX + (slot * (SLOT_SIZE + SLOT_SPACING));

            // Fondo del slot
            guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF1D1D1D);

            // Bordes del slot
            guiGraphics.fill(x, y, x + SLOT_SIZE, y + 1, 0xFF373737);
            guiGraphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF373737);
            guiGraphics.fill(x, y, x + 1, y + SLOT_SIZE, 0xFF373737);
            guiGraphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF373737);

            // Renderizar item
            ItemStack itemstack = player.getInventory().items.get(slot);
            int itemX = x + (SLOT_SIZE - 16) / 2;
            int itemY = y + (SLOT_SIZE - 16) / 2;

            guiGraphics.renderItem(itemstack, itemX, itemY);
            guiGraphics.renderItemDecorations(minecraft.font, itemstack, itemX, itemY);

            // Resaltar slot seleccionado
            if (slot == player.getInventory().selected) {
                guiGraphics.fill(x, y, x + SLOT_SIZE, y + 1, 0xFFFFFFFF);
                guiGraphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFFFFFFFF);
                guiGraphics.fill(x, y, x + 1, y + SLOT_SIZE, 0xFFFFFFFF);
                guiGraphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFFFFFFFF);
            }
        }
    });

    private static void renderHotbarBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Fondo semitransparente negro
        guiGraphics.fill(x, y, x + width, y + height, 0xCC000000);

        // Bordes
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF373737);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF373737);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF373737);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF373737);
    }

    private static void renderPlayerStats(GuiGraphics guiGraphics, int x, int y, Player player, Minecraft minecraft) {
        // Fondo para las barras de estado
        renderHotbarBackground(guiGraphics, x - 5, y - 5,
                BAR_WIDTH + 10, ((BAR_HEIGHT + BAR_SPACING) * 4) + 5);

        // Barras de estado
        renderStatBar(guiGraphics, x, y, "HP", 0xFFE74C3C, player.getHealth() / player.getMaxHealth(), minecraft);
        renderStatBar(guiGraphics, x, y + BAR_HEIGHT + BAR_SPACING, "Food", 0xFFF1C40F, player.getFoodData().getFoodLevel() / 20f, minecraft);
        renderStatBar(guiGraphics, x, y + (BAR_HEIGHT + BAR_SPACING) * 2, "XP", 0xFF2ECC71, player.experienceProgress, minecraft);

        // Barra de armadura
        float armorPercentage = player.getArmorValue() / 20f;
        renderStatBar(guiGraphics, x, y + (BAR_HEIGHT + BAR_SPACING) * 3, "Armor", 0xFF3498DB, armorPercentage, minecraft);
    }

    private static void renderStatBar(GuiGraphics guiGraphics, int x, int y, String label, int color, float percentage, Minecraft minecraft) {
        // Fondo de la barra
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF202020);

        // Barra de progreso
        guiGraphics.fill(x, y, x + (int)(BAR_WIDTH * percentage), y + BAR_HEIGHT, color);

        // Bordes de la barra
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + 1, 0xFF373737);
        guiGraphics.fill(x, y + BAR_HEIGHT - 1, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF373737);
        guiGraphics.fill(x, y, x + 1, y + BAR_HEIGHT, 0xFF373737);
        guiGraphics.fill(x + BAR_WIDTH - 1, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF373737);

        // Etiqueta centrada
        int textWidth = minecraft.font.width(label);
        guiGraphics.drawString(minecraft.font, label, x + (BAR_WIDTH - textWidth) / 2, y + 2, 0xFFFFFFFF);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "custom_hotbar", CUSTOM_HOTBAR);
    }
}