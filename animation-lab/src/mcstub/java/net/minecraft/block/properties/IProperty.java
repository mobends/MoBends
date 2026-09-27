package net.minecraft.block.properties;

public class IProperty<T>
{
    private final String name;
    public IProperty(String name) { this.name = name; }
    public String getName() { return name; }
}
