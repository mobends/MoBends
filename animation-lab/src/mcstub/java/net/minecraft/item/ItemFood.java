package net.minecraft.item;

public class ItemFood extends Item
{
    public ItemFood(String name) { super(name); setUseAction(EnumAction.EAT); }
}
