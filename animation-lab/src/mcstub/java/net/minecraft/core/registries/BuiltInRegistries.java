package net.minecraft.core.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** The lab's items and entity types, under their ids (see {@link Items}, {@link EntityType}). */
public class BuiltInRegistries {
    public static final Registry<Item> ITEM = new Registry<>(ResourceLocation.withDefaultNamespace("air"));
    public static final Registry<EntityType<?>> ENTITY_TYPE = new Registry<>(ResourceLocation.withDefaultNamespace("pig"));

    static {
        // The items and entity types register themselves as their classes load; load them, as the
        // game's bootstrap does, so an id can be looked up before anything uses them.
        load(Items.class);
        load(EntityType.class);
    }

    private static void load(Class<?> type) {
        try {
            Class.forName(type.getName(), true, type.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
