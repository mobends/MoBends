package net.minecraft.world.level.block.state.properties;

import net.minecraft.core.Direction;

public class DirectionProperty extends Property<Direction> {
    protected DirectionProperty(String name) {
        super(name);
    }

    public static DirectionProperty create(String name) {
        return new DirectionProperty(name);
    }
}
