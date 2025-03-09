package com.bitzlay.ebzinventory.client.gui.screen;

import com.bitzlay.ebzinventory.client.gui.components.CategoryButton;
import com.bitzlay.ebzinventory.client.gui.panels.CraftingQueuePanel;
import com.bitzlay.ebzinventory.client.gui.panels.RecipeInfoPanel;
import com.bitzlay.ebzinventory.client.gui.panels.RecipesPanel;
import com.bitzlay.ebzinventory.client.renderer.InventorySlotRenderer;
import com.bitzlay.ebzinventory.crafting.CraftingQueueHandler;
import com.bitzlay.ebzinventory.crafting.CraftingQueueItem;
import com.bitzlay.ebzinventory.model.InventoryRecipe;
import com.bitzlay.ebzinventory.model.ItemCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main inventory screen with Rust-style UI
 */
public class RustStyleInventoryScreen extends AbstractContainerScreen<InventoryMenu> {
    // Dimensions
    private final int textureWidth = 480;
    private final int textureHeight = 480;
    private final int slotSize = 36;
    private final int slotSpacing = 4;
    private static final int CRAFTING_AREA_HEIGHT = 280;

    // UI Components
    private Button craftingButton;
    private InventorySlotRenderer slotRenderer;
    private RecipesPanel recipesPanel;
    private RecipeInfoPanel recipeInfoPanel;
    private CraftingQueuePanel queuePanel;

    // State
    private float xMouse;
    private float yMouse;
    private boolean isMouseDown = false;
    private String currentCategory = "CA";
    private InventoryRecipe selectedRecipe = null;
    private boolean showCrafting = false;

    public RustStyleInventoryScreen(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = textureWidth;
        this.imageHeight = textureHeight;
        this.titleLabelX = -999;
        this.titleLabelY = -999;
        this.inventoryLabelX = -999;
        this.inventoryLabelY = -999;
    }

    @Override
    protected void init() {
        super.init();

        // Calculate screen position
        int windowHeight = this.minecraft.getWindow().getHeight();
        int windowWidth = this.minecraft.getWindow().getWidth();
        float guiScale = (float)this.minecraft.getWindow().getGuiScale();

        this.leftPos = (int)((windowWidth / guiScale - this.imageWidth) / 2);
        this.topPos = (int)((windowHeight / guiScale - this.imageHeight) / 2);

        // Initialize slot renderer
        slotRenderer = new InventorySlotRenderer(slotSize, leftPos, topPos);

        // Add crafting toggle button
        craftingButton = Button.builder(Component.literal("Crafteo"), this::toggleCrafting)
                .pos(this.leftPos + 5, this.topPos + 5)
                .size(60, 20)
                .build();
        this.addRenderableWidget(craftingButton);

        // Position inventory slots
        repositionSlots();
    }

    /**
     * Toggle between inventory and crafting views
     */
    private void toggleCrafting(Button button) {
        showCrafting = !showCrafting;

        if (!showCrafting) {
            // Reset crafting state
            clearWidgets();
            addRenderableWidget(craftingButton);
            selectedRecipe = null;
        }

        if (minecraft.player != null) {
            minecraft.player.playSound(
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                    1.0F,
                    1.0F
            );
        }

        repositionSlots();
    }

    /**
     * Reposition inventory slots based on current view
     */
    private void repositionSlots() {
        if (showCrafting) {
            // Hide all slots in crafting view
            for (Slot slot : this.menu.slots) {
                slotRenderer.setSlotPosition(slot, -999, -999);
            }
            return;
        }

        // Calculate slot positions for inventory view
        int mainInventoryX = (this.width - (9 * (slotSize + slotSpacing))) / 2;
        int mainInventoryY = this.height - (4 * (slotSize + slotSpacing)) - 10;
        int armorStartX = mainInventoryX - (7 * (slotSize + slotSpacing));
        int hotbarY = mainInventoryY + (3 * (slotSize + slotSpacing)) + 4;

        // Position shield slot
        slotRenderer.setSlotPosition(this.menu.slots.get(45),
                armorStartX - this.leftPos,
                hotbarY - this.topPos);

        // Position armor slots
        for (int i = 0; i < 4; i++) {
            slotRenderer.setSlotPosition(this.menu.slots.get(5 + i),
                    armorStartX - this.leftPos + (i + 1) * (slotSize + slotSpacing),
                    hotbarY - this.topPos);
        }

        // Position main inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotRenderer.setSlotPosition(this.menu.slots.get(9 + row * 9 + col),
                        mainInventoryX - this.leftPos + col * (slotSize + slotSpacing),
                        mainInventoryY - this.topPos + row * (slotSize + slotSpacing));
            }
        }

        // Position hotbar slots
        for (int i = 0; i < 9; i++) {
            slotRenderer.setSlotPosition(this.menu.slots.get(36 + i),
                    mainInventoryX - this.leftPos + i * (slotSize + slotSpacing),
                    hotbarY - this.topPos);
        }

        // Hide vanilla crafting slots
        for (int i = 0; i < 5; i++) {
            slotRenderer.setSlotPosition(this.menu.slots.get(i), -999, -999);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        if (showCrafting) {
            renderCraftingArea(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // Render widgets
        for (net.minecraft.client.gui.components.Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * Renders the crafting UI
     */
    private void renderCraftingArea(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int craftingY = this.topPos + 30;

        // Panel dimensions
        int recipesWidth = 220;      // Recipes panel
        int infoWidth = 220;         // Info panel
        int queueWidth = 160;        // Queue panel
        int spacing = 25;            // Spacing between panels

        // Panel heights
        int recipesPanelHeight = CRAFTING_AREA_HEIGHT - 60;
        int infoPanelHeight = CRAFTING_AREA_HEIGHT - 100;
        int queuePanelHeight = CRAFTING_AREA_HEIGHT - 100;

        // Render crafting area background
        slotRenderer.renderSlotGroupBackground(
                guiGraphics, leftPos, craftingY, imageWidth, CRAFTING_AREA_HEIGHT);

        // Render category buttons
        renderCategoryButtons(guiGraphics, leftPos + 15, craftingY + 15);

        // Calculate panel positions
        int recipesX = leftPos + 15;
        int infoX = leftPos + recipesWidth + spacing + 15;
        int queueX = infoX + infoWidth + spacing;

        // Create panels if needed
        if (recipesPanel == null) {
            recipesPanel = new RecipesPanel(
                    this, recipesX, craftingY + 50, recipesWidth, recipesPanelHeight,
                    currentCategory, selectedRecipe, this::selectRecipe);
        }

        if (recipeInfoPanel == null) {
            recipeInfoPanel = new RecipeInfoPanel(
                    this, infoX, craftingY + 50, infoWidth, infoPanelHeight,
                    selectedRecipe, this::startCrafting);
        }

        // Render panels
        recipesPanel.render(guiGraphics, mouseX, mouseY, partialTick);
        recipeInfoPanel.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render queue if there are items
        if (minecraft.player != null) {
            UUID playerUUID = minecraft.player.getUUID();
            List<CraftingQueueItem> queue = CraftingQueueHandler.getPlayerQueue(playerUUID);
            if (!queue.isEmpty()) {
                if (queuePanel == null) {
                    queuePanel = new CraftingQueuePanel(
                            this, queueX, craftingY + 50, queueWidth, queuePanelHeight,
                            playerUUID, this::cancelCrafting, () -> CraftingQueueHandler.clearQueue(playerUUID));
                }

                // Background for queue panel
                slotRenderer.renderSlotGroupBackground(
                        guiGraphics, queueX - 5, craftingY, queueWidth + 10, queuePanelHeight);

                // Render queue
                queuePanel.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    /**
     * Renders the category buttons
     */
    private void renderCategoryButtons(GuiGraphics guiGraphics, int x, int y) {
        int buttonSize = 20;
        int spacing = 5;

        // Clear existing category buttons
        this.renderables.removeIf(renderable ->
                renderable instanceof CategoryButton);

        // Create new category buttons
        for (ItemCategory category : ItemCategory.getAllCategories().values()) {
            CategoryButton categoryButton = new CategoryButton(
                    x, y, buttonSize, buttonSize,
                    category, this::onCategorySelected,
                    category.getId().equals(currentCategory),
                    this);

            this.addRenderableWidget(categoryButton);
            x += buttonSize + spacing;
        }
    }

    /**
     * Handles category selection
     */
    private void onCategorySelected(CategoryButton button) {
        this.currentCategory = button.getCategory().getId();
        this.selectedRecipe = null;

        // Reset panels
        recipesPanel = null;
        recipeInfoPanel = null;

        // Play click sound
        if (minecraft.player != null) {
            minecraft.player.playSound(
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                    1.0F,
                    1.0F
            );
        }
    }

    /**
     * Handles recipe selection
     */
    private void selectRecipe(InventoryRecipe recipe) {
        this.selectedRecipe = recipe == selectedRecipe ? null : recipe;

        // Update info panel
        recipeInfoPanel = null;

        // Play click sound
        if (minecraft.player != null) {
            minecraft.player.playSound(
                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                    1.0F,
                    1.0F
            );
        }
    }

    /**
     * Starts crafting for a recipe
     */
    private void startCrafting(InventoryRecipe recipe) {
        if (!canCraft(recipe)) {
            minecraft.player.displayClientMessage(
                    Component.literal("§cNo tienes suficientes materiales"),
                    false
            );
            return;
        }

        // Consume materials
        consumeMaterials(recipe);

        // Create queue item
        CraftingQueueItem queueItem = new CraftingQueueItem(
                recipe.getId(),
                minecraft.player.getUUID(),
                recipe.getResult(),
                recipe.getCraftingTime(),
                recipe
        );

        // Add to queue
        CraftingQueueHandler.addToQueue(queueItem);

        // Play sound
        minecraft.player.playSound(
                net.minecraft.sounds.SoundEvents.UI_STONECUTTER_TAKE_RESULT,
                1.0F,
                1.0F
        );

        // Reset queue panel to show the new item
        queuePanel = null;
    }

    /**
     * Cancel a crafting item and return materials if not complete
     */
    private void cancelCrafting(int index) {
        UUID playerUUID = minecraft.player.getUUID();
        List<CraftingQueueItem> queue = CraftingQueueHandler.getPlayerQueue(playerUUID);

        if (index < 0 || index >= queue.size()) return;

        CraftingQueueItem item = queue.get(index);

        // Return materials if not complete
        if (item.getProgress() < 1.0f) {
            InventoryRecipe recipe = item.getRecipe();
            for (Map.Entry<Item, Integer> ingredient : recipe.getIngredients().entrySet()) {
                ItemStack returnStack = new ItemStack(ingredient.getKey(), ingredient.getValue());
                if (!minecraft.player.getInventory().add(returnStack)) {
                    minecraft.player.drop(returnStack, false);
                }
            }
        }

        // Cancel the item
        CraftingQueueHandler.cancelItem(playerUUID, index);

        // Reset queue panel
        queuePanel = null;
    }

    /**
     * Check if player has materials for recipe
     */
    private boolean canCraft(InventoryRecipe recipe) {
        if (minecraft.player == null) return false;

        for (Map.Entry<Item, Integer> ingredient : recipe.getIngredients().entrySet()) {
            if (minecraft.player.getInventory().countItem(ingredient.getKey()) < ingredient.getValue()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Consume materials for a recipe
     */
    private void consumeMaterials(InventoryRecipe recipe) {
        if (minecraft.player == null) return;

        for (Map.Entry<Item, Integer> ingredient : recipe.getIngredients().entrySet()) {
            int remaining = ingredient.getValue();
            while (remaining > 0) {
                int slot = minecraft.player.getInventory().findSlotMatchingItem(
                        new ItemStack(ingredient.getKey()));

                if (slot == -1) break;

                ItemStack stack = minecraft.player.getInventory().getItem(slot);
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (showCrafting) return;

        int mainInventoryX = (this.width - (9 * (slotSize + slotSpacing))) / 2;
        int mainInventoryY = this.height - (4 * (slotSize + slotSpacing)) - 10;
        int hotbarY = mainInventoryY + (3 * (slotSize + slotSpacing)) + 4;
        int armorStartX = mainInventoryX - (7 * (slotSize + slotSpacing));

        // Render inventory backgrounds
        slotRenderer.renderSlotGroupBackground(
                guiGraphics, mainInventoryX, mainInventoryY,
                9 * (slotSize + slotSpacing), 3 * (slotSize + slotSpacing));

        slotRenderer.renderSlotGroupBackground(
                guiGraphics, mainInventoryX, hotbarY,
                9 * (slotSize + slotSpacing), slotSize);

        slotRenderer.renderSlotGroupBackground(
                guiGraphics, armorStartX, hotbarY,
                5 * (slotSize + slotSpacing), slotSize);

        // Render individual slots
        renderSlots(guiGraphics);
    }

    /**
     * Render individual slot backgrounds
     */
    private void renderSlots(GuiGraphics guiGraphics) {
        for (Slot slot : this.menu.slots) {
            boolean isArmorSlot = slot == this.menu.slots.get(5) ||
                    slot == this.menu.slots.get(6) ||
                    slot == this.menu.slots.get(7) ||
                    slot == this.menu.slots.get(8) ||
                    slot == this.menu.slots.get(45);

            slotRenderer.renderSlotBackground(guiGraphics, slot, isArmorSlot);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        isMouseDown = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isMouseDown = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Allow movement keys to pass through the UI
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Pass through WASD and space keys for player movement
        if (keyCode == GLFW.GLFW_KEY_W ||
                keyCode == GLFW.GLFW_KEY_A ||
                keyCode == GLFW.GLFW_KEY_S ||
                keyCode == GLFW.GLFW_KEY_D ||
                keyCode == GLFW.GLFW_KEY_SPACE) {
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Allow movement keys to pass through the UI on release
     */
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // Pass through WASD and space keys for player movement
        if (keyCode == GLFW.GLFW_KEY_W ||
                keyCode == GLFW.GLFW_KEY_A ||
                keyCode == GLFW.GLFW_KEY_S ||
                keyCode == GLFW.GLFW_KEY_D ||
                keyCode == GLFW.GLFW_KEY_SPACE) {
            return false;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    /**
     * Clear panels when screen is closed
     */
    @Override
    public void removed() {
        super.removed();
        recipesPanel = null;
        recipeInfoPanel = null;
        queuePanel = null;
    }

    /**
     * Don't pause the game when inventory is open
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Close inventory with escape key
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}