package net.minecraft.world.level.block.state.properties;

public abstract class Property<T extends Comparable<T>> {
    private final String name;

    protected Property(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
