package net.minecraft.block;

import net.minecraft.block.properties.PropertyDirection;

public class BlockLadder extends Block
{
    public static final PropertyDirection FACING = new PropertyDirection("facing");
    public BlockLadder(String name) { super(name); }
}
