package goblinbob.mobends;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The plugin of {@code mobends.mixins.json}, which lists its mixins here rather than in the JSON:
 * the classes they target differ between Minecraft versions, and a version's list is fenced as
 * code is (a JSON file can't be). The client's are listed only on a client, as the config's own
 * {@code client} list would.
 */
public final class MoBendsMixins implements IMixinConfigPlugin {
    /** On both sides. */
    private static final List<String> COMMON = List.of(
        "PlayerListMixin"
    );

    /** On the client only. */
    private static final List<String> CLIENT = List.of(
        //? if >=26.1 {
        /*"CapeLayerMixin",
        "ClientPacketListenerMixin",
        "CustomHeadLayerMixin",
        "EntityRenderStateMixin",
        "GameRendererMixin",
        "GuiGraphicsExtractorAccessor",
        "HumanoidArmorLayerMixin",
        "HumanoidModelMixin",
        "ItemInHandLayerMixin",
        "LayerDefinitionMixin",
        "LivingEntityAccessor",
        "LivingEntityRendererMixin",
        "MinecraftMixin",
        "ModelFeatureRendererMixin",
        "ModelPartMixin",
        "OptionsMixin",
        "PlayerAccessor",
        "PlayerItemInHandLayerMixin",
        "PlayerModelMixin",
        "PlayerRendererMixin",
        "SubmitNodeCollectionMixin"
        *///?} else {
        "AgeableListModelMixin",
        "AgeableModelRootMixin",
        "ArrowRendererMixin",
        "CapeLayerMixin",
        "ClientPacketListenerMixin",
        "CustomHeadLayerMixin",
        "ElytraLayerMixin",
        "GameRendererMixin",
        "HierarchicalModelMixin",
        "HumanoidArmorLayerMixin",
        "HumanoidModelMixin",
        "IronGolemFlowerLayerMixin",
        "IronGolemRendererMixin",
        "ItemInHandLayerMixin",
        "KeyMappingAccessor",
        "LayerDefinitionMixin",
        "LevelRendererMixin",
        "ListModelMixin",
        "LivingEntityRendererMixin",
        "MinecraftMixin",
        "ModelPartMixin",
        "MushroomCowMushroomLayerMixin",
        "OptionsMixin",
        "PlayerAccessor",
        "PlayerItemInHandLayerMixin",
        "PlayerModelMixin",
        "PlayerRendererMixin",
        "StuckInBodyLayerMixin",
        "WitchItemLayerMixin",
        "WolfRendererMixin"
        //?}
    );

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        List<String> mixins = new ArrayList<>(COMMON);
        if (MixinEnvironment.getCurrentEnvironment().getSide() == MixinEnvironment.Side.CLIENT) {
            mixins.addAll(CLIENT);
            // NeoForge gives each of the GUI's pictures (an entity, say) a renderer of its own already.
            //? if >=26.1 && fabric {
            /*mixins.add("GuiRendererMixin");
            *///?}
        }
        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
