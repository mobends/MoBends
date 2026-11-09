package net.minecraft.world.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/** The items the lab's scenarios hold, under the game's ids. */
public class Items {
    public static final Item AIR = register("air", new Item(new Item.Properties()));
    public static final Item IRON_SWORD = register("iron_sword", new SwordItem(new Item.Properties()));
    public static final Item IRON_PICKAXE = register("iron_pickaxe", new Item(new Item.Properties()));
    public static final Item BOW = register("bow", new BowItem(new Item.Properties()));
    public static final Item APPLE = register("apple", new Item(new Item.Properties().food()));
    public static final Item SHIELD = register("shield", new ShieldItem(new Item.Properties()));
    public static final Item TORCH = register("torch", new Item(new Item.Properties()));

    private static Item register(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, "minecraft:" + id, item);
    }
}
