package net.minecraft.core;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/** Stand-in for the game's registries (an interface there): the id each thing is registered under. */
public class Registry<T> {
    private final Map<T, ResourceLocation> keys = new IdentityHashMap<>();
    @Nullable
    private final ResourceLocation defaultKey;

    /** @param defaultKey what {@link #getKey} gives for something not registered (a defaulted registry's), or null */
    public Registry(@Nullable ResourceLocation defaultKey) {
        this.defaultKey = defaultKey;
    }

    public static <T> T register(Registry<? super T> registry, String id, T value) {
        ResourceLocation key = ResourceLocation.parse(id);
        registry.keys.put(value, key);
        return value;
    }

    public ResourceLocation getKey(T value) {
        ResourceLocation key = keys.get(value);
        return key != null ? key : defaultKey;
    }
}
