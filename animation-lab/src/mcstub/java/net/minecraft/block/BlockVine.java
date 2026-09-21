package net.minecraft.block;

import net.minecraft.block.properties.PropertyBool;

public class BlockVine extends Block
{
    public static final PropertyBool NORTH = new PropertyBool("north");
    public static final PropertyBool SOUTH = new PropertyBool("south");
    public static final PropertyBool WEST = new PropertyBool("west");
    public static final PropertyBool EAST = new PropertyBool("east");
    public BlockVine(String name) { super(name); }
}
