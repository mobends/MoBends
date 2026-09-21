package net.minecraft.init;

import net.minecraft.item.*;

public class Items
{
    public static final Item AIR = new Item("air");
    public static final Item IRON_SWORD = new ItemSword("iron_sword");
    public static final Item IRON_PICKAXE = new Item("iron_pickaxe");
    public static final Item BOW = new ItemBow("bow");
    public static final Item APPLE = new ItemFood("apple");
    public static final Item SHIELD = new Item("shield").setUseAction(EnumAction.BLOCK);
}
