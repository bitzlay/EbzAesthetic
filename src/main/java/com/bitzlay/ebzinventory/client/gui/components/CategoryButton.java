package com.bitzlay.ebzinventory.client.gui.components;

import com.bitzlay.ebzinventory.model.ItemCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Button representing a recipe category in the crafting UI
 */
public class CategoryButton extends AbstractWidget {
    private final ItemCategory category;
    private final Consumer<CategoryButton> onPress;
    private final boolean selected;
    private final Screen parentScreen;

    public CategoryButton(int x, int y, int width, int height, ItemCategory category,
                          Consumer<CategoryButton> onPress, boolean selected, Screen parentScreen) {
        super(x, y, width, height, Component.literal(category.getName()));
        this.category = category;
        this.onPress = onPress;
        this.selected = selected;
        this.parentScreen = parentScreen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render button background with selection state
        int backgroundColor = selected ? 0xFF4A4A4A : isHovered ? 0xFF3F3F3F : 0xFF333333;
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, backgroundColor);

        // Render border
        renderBorder(guiGraphics);

        // Render icon
        ItemStack iconStack = new ItemStack(category.getIcon());
        guiGraphics.renderItem(iconStack, getX() + (width - 16) / 2, getY() + (height - 16) / 2);

        // Render tooltip if hovered
        if (isHovered && parentScreen != null) {
            guiGraphics.renderTooltip(
                    parentScreen.getMinecraft().font,
                    Component.literal(category.getName()),
                    mouseX, mouseY
            );
        }
    }

    private void renderBorder(GuiGraphics guiGraphics) {
        int borderColor = selected ? 0xFF777777 : 0xFF555555;

        // Top border
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + 1, borderColor);
        // Bottom border
        guiGraphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, borderColor);
        // Left border
        guiGraphics.fill(getX(), getY(), getX() + 1, getY() + height, borderColor);
        // Right border
        guiGraphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, borderColor);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (onPress != null) {
            onPress.accept(this);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public ItemCategory getCategory() {
        return category;
    }
}