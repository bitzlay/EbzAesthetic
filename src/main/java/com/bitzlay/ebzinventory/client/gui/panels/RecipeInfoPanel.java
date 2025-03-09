package com.bitzlay.ebzinventory.client.gui.panels;

import com.bitzlay.ebzinventory.crafting.CraftingQueueHandler;
import com.bitzlay.ebzinventory.crafting.CraftingQueueItem;
import com.bitzlay.ebzinventory.model.InventoryRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Panel showing detailed information about the selected recipe
 */
public class RecipeInfoPanel extends BasePanel {
    private final InventoryRecipe selectedRecipe;
    private final Consumer<InventoryRecipe> onStartCrafting;

    public RecipeInfoPanel(Screen parentScreen, int x, int y, int width, int height,
                           InventoryRecipe selectedRecipe,
                           Consumer<InventoryRecipe> onStartCrafting) {
        super(parentScreen, x, y, width, height, "Información");
        this.selectedRecipe = selectedRecipe;
        this.onStartCrafting = onStartCrafting;
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        clearWidgets();

        if (selectedRecipe == null) {
            guiGraphics.drawString(parentScreen.getMinecraft().font,
                    "Selecciona una receta", x + 10, y + 30, 0xAAAAAA);
            return;
        }

        int contentX = x + 10;
        int contentY = y + 20;
        int spacing = 16;

        // Render recipe name
        guiGraphics.drawString(parentScreen.getMinecraft().font,
                selectedRecipe.getDisplayName(), contentX, contentY, 0xFFFFFF);
        contentY += spacing + 5;

        // Render result item
        guiGraphics.renderItem(selectedRecipe.getResult(), contentX, contentY);
        guiGraphics.drawString(parentScreen.getMinecraft().font,
                selectedRecipe.getResult().getHoverName().getString(),
                contentX + 25, contentY + 5, 0xFFFFFF);
        contentY += spacing + 10;

        // Render materials title
        guiGraphics.drawString(parentScreen.getMinecraft().font,
                "Materiales:", contentX, contentY, 0xFFFFFF);
        contentY += spacing;

        // Render materials in two columns
        renderIngredients(guiGraphics, contentX, contentY);

        // Add crafting button
        addCraftingButton(guiGraphics);
    }

    private void renderIngredients(GuiGraphics guiGraphics, int startX, int startY) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        Map<Item, Integer> ingredients = selectedRecipe.getIngredients();
        int itemsPerColumn = (ingredients.size() + 1) / 2;
        int currentColumn = 0;
        int currentRow = 0;
        int columnWidth = (width - 30) / 2;

        for (Map.Entry<Item, Integer> ingredient : ingredients.entrySet()) {
            int itemX = startX + (currentColumn * columnWidth);
            int itemY = startY + (currentRow * 20);

            ItemStack ingredientStack = new ItemStack(ingredient.getKey());
            guiGraphics.renderItem(ingredientStack, itemX, itemY);

            int playerHas = player.getInventory().countItem(ingredient.getKey());
            String countText = playerHas + "/" + ingredient.getValue();
            int color = playerHas >= ingredient.getValue() ? 0x55FF55 : 0xFF5555;

            guiGraphics.drawString(parentScreen.getMinecraft().font,
                    countText, itemX + 25, itemY + 5, color);

            currentRow++;
            if (currentRow >= itemsPerColumn) {
                currentRow = 0;
                currentColumn++;
            }
        }
    }

    private void addCraftingButton(GuiGraphics guiGraphics) {
        if (selectedRecipe == null || Minecraft.getInstance().player == null) return;

        UUID playerUUID = Minecraft.getInstance().player.getUUID();
        boolean canCraft = canCraft(selectedRecipe);
        List<CraftingQueueItem> queue = CraftingQueueHandler.getPlayerQueue(playerUUID);

        String buttonText;
        boolean enableButton;

        if (queue.size() >= 5) {
            buttonText = "Cola llena";
            enableButton = false;
        } else if (!canCraft) {
            buttonText = "No hay materiales";
            enableButton = false;
        } else {
            buttonText = "Craftear";
            enableButton = true;
        }

        Button craftButton = Button.builder(
                        Component.literal(buttonText),
                        button -> {
                            if (enableButton && onStartCrafting != null) {
                                onStartCrafting.accept(selectedRecipe);
                            }
                        })
                .pos(x + (width - 100) / 2, y + height - 30)
                .size(100, 20)
                .build();

        craftButton.active = enableButton;
        addWidget(craftButton);
    }

    private boolean canCraft(InventoryRecipe recipe) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;

        for (Map.Entry<Item, Integer> ingredient : recipe.getIngredients().entrySet()) {
            if (player.getInventory().countItem(ingredient.getKey()) < ingredient.getValue()) {
                return false;
            }
        }

        return true;
    }
}