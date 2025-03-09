package com.bitzlay.ebzinventory.client.gui.panels;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * Base abstract class for all UI panels
 */
public abstract class BasePanel {
    protected final int x;
    protected final int y;
    protected final int width;
    protected final int height;
    protected final String title;
    protected final Screen parentScreen;

    protected final List<AbstractWidget> widgets = new ArrayList<>();

    protected BasePanel(Screen parentScreen, int x, int y, int width, int height, String title) {
        this.parentScreen = parentScreen;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
    }

    /**
     * Renders the panel background and title
     */
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render panel background
        renderPanelBackground(guiGraphics);

        // Render panel title
        guiGraphics.drawString(parentScreen.getMinecraft().font, title, x + 5, y + 5, 0xFFFFFF);

        // Render panel content
        renderContent(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * Renders the panel background with borders
     */
    protected void renderPanelBackground(GuiGraphics guiGraphics) {
        // Fondo del panel
        guiGraphics.fill(x, y, x + width, y + height, 0xAA000000);

        // Borde del panel
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF555555);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF555555);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF555555);
    }

    /**
     * Renders the panel content (implemented by subclasses)
     */
    protected abstract void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    /**
     * Checks if a point is within this panel
     */
    public boolean isInBounds(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /**
     * Adds a widget to this panel and the parent screen
     */
    protected void addWidget(AbstractWidget widget) {
        widgets.add(widget);
        // Since addRenderableWidget is protected, we'll use reflection to access it
        try {
            java.lang.reflect.Method addWidgetMethod = Screen.class.getDeclaredMethod(
                    "addRenderableWidget", net.minecraft.client.gui.components.events.GuiEventListener.class);
            addWidgetMethod.setAccessible(true);
            addWidgetMethod.invoke(parentScreen, widget);
        } catch (Exception e) {
            // Fallback: just add to renderables list if reflection fails
            try {
                java.lang.reflect.Field renderablesField = Screen.class.getDeclaredField("renderables");
                renderablesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<net.minecraft.client.gui.components.Renderable> renderables =
                        (List<net.minecraft.client.gui.components.Renderable>) renderablesField.get(parentScreen);
                renderables.add(widget);

                java.lang.reflect.Field childrenField = Screen.class.getDeclaredField("children");
                childrenField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<net.minecraft.client.gui.components.events.GuiEventListener> children =
                        (List<net.minecraft.client.gui.components.events.GuiEventListener>) childrenField.get(parentScreen);
                children.add(widget);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Removes all widgets from this panel
     */
    public void clearWidgets() {
        widgets.clear();
    }

    /**
     * Gets all widgets in this panel
     */
    public List<GuiEventListener> getWidgets() {
        return new ArrayList<>(widgets);
    }

    /**
     * Called when mouse is clicked within this panel
     * @return true if the click was handled
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}