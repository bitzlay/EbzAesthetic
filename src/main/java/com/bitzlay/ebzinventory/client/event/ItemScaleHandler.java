package com.bitzlay.ebzinventory.client.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.bitzlay.ebzinventory.client.gui.screen.RustStyleInventoryScreen;
import net.minecraftforge.fml.common.Mod;
import com.bitzlay.ebzinventory.EbzInventory;

@Mod.EventBusSubscriber(modid = EbzInventory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemScaleHandler {

    @SubscribeEvent
    public static void onGuiRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof RustStyleInventoryScreen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();

            // Ajustamos la escala basándonos en la GUI Scale actual
            float targetScale = 1.75f / (scale / 2);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(targetScale, targetScale, targetScale);
            // La transformación se aplicará aquí
            guiGraphics.pose().popPose();
        }
    }
}