package net.minecraft.item;

import net.minecraft.init.Items;

public class ItemStack
{
    public static final ItemStack EMPTY = new ItemStack(Items.AIR);

    private final Item item;
    private String displayName;

    public ItemStack(Item item)
    {
        this.item = item;
        this.displayName = item.getName();
    }

    public ItemStack setDisplayName(String name) { this.displayName = name; return this; }
    public Item getItem() { return item; }
    public boolean isEmpty() { return item == Items.AIR; }
    public EnumAction getItemUseAction() { return item.getItemUseAction(this); }
    public String getDisplayName() { return displayName; }
}
