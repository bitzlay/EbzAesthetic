package com.bitzlay.ebzinventory.crafting;



import com.bitzlay.ebzinventory.model.InventoryRecipe;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.item.Item;

import net.minecraft.world.item.ItemStack;

import net.minecraft.network.chat.Component;

import net.minecraft.client.Minecraft;

import net.minecraftforge.event.TickEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;

import com.bitzlay.ebzinventory.EbzInventory;

import net.minecraftforge.server.ServerLifecycleHooks;



import java.util.*;



@Mod.EventBusSubscriber(modid = EbzInventory.MOD_ID)

public class CraftingQueueHandler {

    private static final Map<UUID, List<CraftingQueueItem>> playerQueues = new HashMap<>();

    private static final int MAX_QUEUE_SIZE = 5;



    public static void addToQueue(CraftingQueueItem item) {

        List<CraftingQueueItem> queue = playerQueues.computeIfAbsent(

                item.getPlayerId(), k -> new ArrayList<>());



        if (queue.size() >= MAX_QUEUE_SIZE) {

            Minecraft.getInstance().player.displayClientMessage(

                    Component.literal("§cLa cola de crafteo está llena"),

                    false

            );

            return;

        }



        if (!queue.isEmpty()) {

            item.pause();

        }



        queue.add(item);

    }



    public static List<CraftingQueueItem> getPlayerQueue(UUID playerId) {

        return playerQueues.getOrDefault(playerId, new ArrayList<>());

    }



    @SubscribeEvent

    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;



        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) return;



        List<CraftingQueueItem> queue = getPlayerQueue(minecraft.player.getUUID());

        if (queue.isEmpty()) return;



        CraftingQueueItem firstItem = queue.get(0);

        if (firstItem.isPaused()) {

            firstItem.resume();

        }



        if (firstItem.isCompleted()) {

            // Entregar item

            ItemStack result = firstItem.getResult();

            if (!minecraft.player.getInventory().add(result)) {

                minecraft.player.drop(result, false);

            }



            // Mensaje y sonido

            minecraft.player.displayClientMessage(

                    Component.literal("§aCrafteo completado: " + result.getHoverName().getString()),

                    false

            );

            minecraft.player.playSound(

                    net.minecraft.sounds.SoundEvents.UI_STONECUTTER_TAKE_RESULT,

                    1.0F,

                    1.0F

            );



            // Remover de la cola

            queue.remove(0);



            // Iniciar siguiente item si existe

            if (!queue.isEmpty()) {

                queue.get(0).resume();

            }

        }

    }



    public static void cancelItem(UUID playerId, int index) {

        List<CraftingQueueItem> queue = playerQueues.get(playerId);

        if (queue != null && index >= 0 && index < queue.size()) {

            queue.remove(index);

            // Si quedan items y estaba pausado el siguiente, lo reanudamos

            if (!queue.isEmpty() && index == 0) {

                queue.get(0).resume();

            }

        }

    }

    public static void clearQueue(UUID playerId) {

        List<CraftingQueueItem> queue = playerQueues.get(playerId);

        if (queue != null) {

            // Devolver materiales de todos los items no completados

            for (CraftingQueueItem item : queue) {

                if (item.getProgress() < 1.0f) {

                    returnMaterials(item.getRecipe(), playerId);

                }

            }

            queue.clear();

        }

    }



    private static void returnMaterials(InventoryRecipe recipe, UUID playerId) {

        ServerPlayer player = ServerLifecycleHooks.getCurrentServer()

                .getPlayerList().getPlayer(playerId);

        if (player != null) {

            for (Map.Entry<Item, Integer> ingredient : recipe.getIngredients().entrySet()) {

                ItemStack returnStack = new ItemStack(ingredient.getKey(), ingredient.getValue());

                if (!player.getInventory().add(returnStack)) {

                    player.drop(returnStack, false);

                }

            }

        }

    }

}