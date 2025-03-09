package com.bitzlay.ebzinventory.client.gui.panels;

import com.bitzlay.ebzinventory.client.gui.components.InventoryRecipeButton;
import com.bitzlay.ebzinventory.model.InventoryRecipe;
import com.bitzlay.ebzinventory.recipe.InventoryRecipeManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Panel displaying available recipes for the current category
 */
public class RecipesPanel extends BasePanel {
    private final String categoryId;
    private final InventoryRecipe selectedRecipe;
    private final Consumer<InventoryRecipe> onRecipeSelected;

    private int currentPage = 0;
    private static final int RECIPES_PER_PAGE = 6;

    public RecipesPanel(Screen parentScreen, int x, int y, int width, int height,
                        String categoryId, InventoryRecipe selectedRecipe,
                        Consumer<InventoryRecipe> onRecipeSelected) {
        super(parentScreen, x, y, width, height, "Recetas Disponibles");
        this.categoryId = categoryId;
        this.selectedRecipe = selectedRecipe;
        this.onRecipeSelected = onRecipeSelected;
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        List<InventoryRecipe> recipes = InventoryRecipeManager.getRecipesByCategory(categoryId);

        if (recipes.isEmpty()) {
            guiGraphics.drawString(parentScreen.getMinecraft().font,
                    "No hay recetas disponibles", x + 10, y + 30, 0xAAAAAA);
            return;
        }

        // Clear existing widgets
        clearWidgets();

        // Calculate pagination
        int totalPages = Math.max(1, (recipes.size() - 1) / RECIPES_PER_PAGE + 1);
        if (currentPage >= totalPages) {
            currentPage = 0;
        }

        int startIndex = currentPage * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, recipes.size());

        // Recipe button dimensions
        int buttonWidth = (width - 20) / 2 - 2;
        int buttonHeight = 36;
        int spacing = 4;
        int contentStartY = y + 25;
        int maxTextWidth = (buttonWidth/2) - spacing - 45;

        // Add recipe buttons
        for (int i = startIndex; i < endIndex; i++) {
            int row = (i - startIndex) / 2;
            int col = (i - startIndex) % 2;
            int buttonX = x + 10 + (col * (buttonWidth + spacing));
            int buttonY = contentStartY + (row * (buttonHeight + spacing));

            InventoryRecipe recipe = recipes.get(i);
            String truncatedName = parentScreen.getMinecraft().font.plainSubstrByWidth(
                    recipe.getDisplayName(), maxTextWidth);

            InventoryRecipeButton recipeButton = new InventoryRecipeButton(
                    buttonX, buttonY, buttonWidth, buttonHeight,
                    recipe.getId(), recipe.getResult(),
                    button -> onRecipeSelected.accept(recipe),
                    recipe == selectedRecipe,
                    truncatedName
            );

            addWidget(recipeButton);
        }

        // Add navigation buttons if needed
        if (totalPages > 1) {
            int navButtonY = y + height - 25;
            int navButtonWidth = 60;

            if (currentPage > 0) {
                Button prevButton = Button.builder(Component.literal("< Anterior"), b -> {
                    currentPage--;
                    clearWidgets();
                }).pos(x + 10, navButtonY).size(navButtonWidth, 20).build();
                addWidget(prevButton);
            }

            if (currentPage < totalPages - 1) {
                Button nextButton = Button.builder(Component.literal("Siguiente >"), b -> {
                    currentPage++;
                    clearWidgets();
                }).pos(x + width - navButtonWidth - 10, navButtonY).size(navButtonWidth, 20).build();
                addWidget(nextButton);
            }
        }
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }
}