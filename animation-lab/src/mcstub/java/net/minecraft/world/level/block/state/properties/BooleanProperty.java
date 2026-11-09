package net.minecraft.world.level.block.state.properties;

public class BooleanProperty extends Property<Boolean> {
    protected BooleanProperty(String name) {
        super(name);
    }

    public static BooleanProperty create(String name) {
        return new BooleanProperty(name);
    }
}
