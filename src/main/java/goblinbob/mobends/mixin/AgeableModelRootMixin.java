package goblinbob.mobends.mixin;

// 26.x's models all keep their root (Model#root).
//? if <26.1 {
import com.llamalad7.mixinextras.sugar.Local;
import goblinbob.mobends.core.vanilla.ModelRootHolder;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.TadpoleModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the root of the parts of the vanilla models that are drawn as a list of parts
 * ({@code AgeableListModel}, {@code ListModel}), which vanilla drops once it has its parts, as
 * {@link HumanoidModelMixin} does for the humanoids': the wolf's; the quadrupeds' (cow, pig, sheep
 * and its wool, polar bear, panda, goat, turtle); the chicken's, axolotl's, bee's, fox's,
 * hoglin's (and zoglin's), horse's (and the donkey's, mule's, the skeleton and zombie horses'),
 * ocelot's (and cat's), tadpole's, and the shulker's. A model of another kind that keeps no root
 * ({@code HierarchicalModel} keeps its own) can't be bent by a definition.
 */
@Mixin({ WolfModel.class, QuadrupedModel.class, ChickenModel.class, AxolotlModel.class, BeeModel.class, FoxModel.class,
        HoglinModel.class, HorseModel.class, OcelotModel.class, TadpoleModel.class, ShulkerModel.class })
public abstract class AgeableModelRootMixin implements ModelRootHolder {
    @Unique
    private ModelPart mobends$root;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mobends$keepRoot(CallbackInfo ci, @Local(argsOnly = true) ModelPart root) {
        mobends$root = root;
    }

    @Override
    public ModelPart mobends$getRoot() {
        return mobends$root;
    }
}
//?}
