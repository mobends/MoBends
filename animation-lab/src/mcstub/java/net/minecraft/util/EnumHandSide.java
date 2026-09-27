package net.minecraft.util;

public enum EnumHandSide
{
    LEFT, RIGHT;

    public EnumHandSide opposite() { return this == LEFT ? RIGHT : LEFT; }
}
