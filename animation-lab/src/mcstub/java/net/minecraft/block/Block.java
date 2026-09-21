package net.minecraft.block;

import net.minecraft.block.state.IBlockState;

public class Block
{
    private final String name;
    private final IBlockState defaultState;

    public Block(String name)
    {
        this.name = name;
        this.defaultState = new IBlockState(this);
    }

    public String getName() { return name; }
    public IBlockState getDefaultState() { return defaultState; }
}
