package com.bitzlay.ebzinventory;

import com.bitzlay.ebzinventory.client.gui.screen.RustStyleInventoryScreen;
import com.bitzlay.ebzinventory.recipe.InventoryRecipeManager;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(EbzInventory.MOD_ID)
public class EbzInventory {
    public static final String MOD_ID = "ebzinventory";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EbzInventory() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        InventoryRecipeManager.loadRecipes("config/inventory_recipes.json");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onScreenOpen(ScreenEvent.Opening event) {
            if (event.getScreen() instanceof InventoryScreen && !(event.getScreen() instanceof RustStyleInventoryScreen)) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    event.setNewScreen(new RustStyleInventoryScreen(player.inventoryMenu, player.getInventory(), player.getDisplayName()));
                }
            }
        }


        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
            // Cancela el renderizado de la hotbar y barras de estado vanilla
            if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type() ||
                    event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type() ||
                    event.getOverlay() == VanillaGuiOverlay.ARMOR_LEVEL.type() ||
                    event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type() ||
                    event.getOverlay() == VanillaGuiOverlay.EXPERIENCE_BAR.type()) {
                event.setCanceled(true);
            }
        }
    }
}