package net.minecraft.core.component;

/** A kind of data an item stack can carry ({@link DataComponents}). */
public final class DataComponentType<T> {
    private final String name;

    public DataComponentType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
