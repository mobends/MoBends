package net.minecraft.item;

import net.minecraft.block.Block;

import java.util.HashMap;
import java.util.Map;

public class Item
{
    private static final Map<Block, Item> BLOCK_ITEMS = new HashMap<>();

    /** Minimal stand-in for the game's item registry: names are "minecraft:<name>". */
    public static final Registry REGISTRY = new Registry();

    public static class Registry
    {
        public net.minecraft.util.ResourceLocation getNameForObject(Item item)
        {
            String name = item.getName().startsWith("block.") ? item.getName().substring(6) : item.getName();
            return new net.minecraft.util.ResourceLocation("minecraft", name);
        }
    }

    private final String name;
    private EnumAction useAction = EnumAction.NONE;

    public Item(String name) { this.name = name; }

    public Item setUseAction(EnumAction action) { this.useAction = action; return this; }
    public EnumAction getItemUseAction(ItemStack stack) { return useAction; }
    public String getName() { return name; }

    public static Item getItemFromBlock(Block block)
    {
        return BLOCK_ITEMS.computeIfAbsent(block, b -> new Item("block." + b.getName()));
    }

    @Override
    public String toString() { return "Item[" + name + "]"; }
}
