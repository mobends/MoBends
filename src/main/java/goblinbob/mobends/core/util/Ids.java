package goblinbob.mobends.core.util;

import net.minecraft.resources.ResourceLocation;

/** Makes resource locations the same way on every Minecraft version (1.21 replaced the constructors with factories). */
public final class Ids {
    private Ids() {
    }

    /** {@code namespace:path}, or {@code minecraft:path} with no namespace; throws if it isn't a valid location. */
    public static ResourceLocation parse(String location) {
        //? if >=1.21 {
        return ResourceLocation.parse(location);
        //?} else {
        /*return new ResourceLocation(location);
        *///?}
    }

    /** {@code namespace:path}; throws if it isn't a valid location. */
    public static ResourceLocation of(String namespace, String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*return new ResourceLocation(namespace, path);
        *///?}
    }

    /** {@code minecraft:path}. */
    public static ResourceLocation minecraft(String path) {
        //? if >=1.21 {
        return ResourceLocation.withDefaultNamespace(path);
        //?} else {
        /*return new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, path);
        *///?}
    }
}
