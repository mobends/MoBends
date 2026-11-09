package goblinbob.mobends.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/** Where each category of keys goes in the Controls screen: Mo' Bends' needs a place (see {@code KeyboardHandler}). */
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    @Accessor("CATEGORY_SORT_ORDER")
    static Map<String, Integer> mobends$getCategorySortOrder() {
        throw new AssertionError();
    }
}
