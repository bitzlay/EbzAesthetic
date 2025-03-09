package com.bitzlay.ebzinventory.model;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a category of crafting recipes
 */
public class ItemCategory {
    private static final Map<String, ItemCategory> CATEGORIES = new HashMap<>();

    static {
        CATEGORIES.put("CA", new ItemCategory("CA", "Armas", Items.IRON_SWORD));
        CATEGORIES.put("CB", new ItemCategory("CB", "Bloques", Items.STONE));
        CATEGORIES.put("CC", new ItemCategory("CC", "Componentes", Items.REDSTONE));
        CATEGORIES.put("CD", new ItemCategory("CD", "Decoración", Items.PAINTING));
        CATEGORIES.put("CM", new ItemCategory("CM", "Mecánica", Items.PISTON));
    }

    private final String id;
    private final String name;
    private final Item icon;

    public ItemCategory(String id, String name, Item icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Item getIcon() {
        return icon;
    }

    public static ItemCategory getCategory(String id) {
        return CATEGORIES.get(id);
    }

    public static Map<String, ItemCategory> getAllCategories() {
        return CATEGORIES;
    }
}