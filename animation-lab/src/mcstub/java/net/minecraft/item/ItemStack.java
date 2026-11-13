package net.minecraft.item;

/** 1.8.9's stack: an empty slot holds null (there is no empty stack). */
public class ItemStack
{
    public int stackSize = 1;
    private final Item item;
    private String displayName;

    public ItemStack(Item item)
    {
        this.item = item;
        this.displayName = item.getName();
    }

    public ItemStack setStackDisplayName(String name) { this.displayName = name; return this; }
    public Item getItem() { return item; }
    public EnumAction getItemUseAction() { return item.getItemUseAction(this); }
    public String getDisplayName() { return displayName; }
}
