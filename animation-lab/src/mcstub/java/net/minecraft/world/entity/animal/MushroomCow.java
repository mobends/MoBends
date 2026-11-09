package net.minecraft.world.entity.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class MushroomCow extends Cow {
    public MushroomCow(EntityType<? extends MushroomCow> type, Level level) {
        super(type, level);
    }

    public MushroomCow(Level level) {
        this(EntityType.MOOSHROOM, level);
    }
}
