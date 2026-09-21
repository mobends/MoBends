package net.minecraft.item;

import net.minecraft.block.Block;

import java.util.HashMap;
import java.util.Map;

public class Item
{
    private static final Map<Block, Item> BLOCK_ITEMS = new HashMap<>();

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
