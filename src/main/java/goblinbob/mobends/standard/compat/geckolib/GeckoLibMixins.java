package goblinbob.mobends.standard.compat.geckolib;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The plugin of {@code mobends.geckolib.mixins.json}: lists its mixins only if GeckoLib is installed
 * (its classes are there), so without it Mixin doesn't even look for their targets. It only looks
 * for GeckoLib's class file, and loads nothing of GeckoLib's.
 */
public final class GeckoLibMixins implements IMixinConfigPlugin {
    //? if >=26.1 {
    /*private static final String ARMOR_RENDERER = "com/geckolib/renderer/GeoArmorRenderer.class";
    /^* GeckoLib 5's armour: noted as it's submitted, its bones drawn bent later (see GeoArmorBones). ^/
    private static final List<String> MIXINS = List.of("GeoArmorRendererMixin", "GeoBoneMixin");
    *///?} else {
    private static final String ARMOR_RENDERER = "software/bernie/geckolib/renderer/GeoArmorRenderer.class";
    private static final List<String> MIXINS = List.of("GeoArmorRendererMixin");
    //?}

    private boolean present;

    @Override
    public void onLoad(String mixinPackage) {
        present = GeckoLibMixins.class.getClassLoader().getResource(ARMOR_RENDERER) != null;
    }

    @Override
    public List<String> getMixins() {
        return present ? MIXINS : Collections.emptyList();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return present;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
