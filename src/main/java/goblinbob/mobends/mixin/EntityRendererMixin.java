package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import goblinbob.mobends.core.client.BendsRenderer;
import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.standard.compat.pal.PalOperations;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/^*
 * Animates a living entity Mo' Bends bends (26.x) once its render state is filled: every renderer's
 * {@code extractRenderState} (a subclass's runs after its superclass's) and what other mods add to
 * it, so the vanilla model can be posed from the whole state for {@code core:vanilla_pose}, as it
 * will be drawn. The state carries the data it's bent by ({@link BentRenderState}).
 ^/
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(
        method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
        at = @At("RETURN")
    )
    private void mobends$pose(Entity entity, float partialTicks, CallbackInfoReturnable<EntityRenderState> cir) {
        if (!((Object) this instanceof LivingEntityRenderer<?, ?, ?> renderer) || !(entity instanceof LivingEntity living)
                || !(cir.getReturnValue() instanceof LivingEntityRenderState state)) {
            return;
        }
        EntityModel<?> model = renderer.getModel();
        // A renderer with a baby model of its own picks the model only as it submits the entity: the
        // one it will draw is the adult's or the baby's, whichever drew last. A baby drawn with a model
        // of its own (26.x's, shaped and textured as its own) isn't the model a definition bends, made
        // for the adult: it's drawn as vanilla, until definitions of the baby models exist.
        if (renderer instanceof AgeableMobRendererAccessor ageable) {
            EntityModel<?> adult = ageable.mobends$adultModel();
            if (living.isBaby() && ageable.mobends$babyModel() != adult) {
                ((BentRenderState) state).mobends$setBent(null, null);
                return;
            }
            model = adult;
        }
        EntityModel<?> posed = model;
        ((BentRenderState) state).mobends$setBent(
                BendsRenderer.beforeRender(living, model, partialTicks, () -> mobends$poseVanilla(living, posed, state)), model);
    }

    /^*
     * Poses {@code model} as vanilla will when it draws the entity (its {@code setupAnim} from the
     * state, with what other mods do there), ahead of its time. The model is shared by every entity
     * its renderer draws, and posed again before each draw, so this leaves nothing behind.
     ^/
    @Unique
    @SuppressWarnings("unchecked")
    private static void mobends$poseVanilla(LivingEntity entity, EntityModel<?> model, LivingEntityRenderState state) {
        PalOperations.beforeVanillaPose(entity);
        ((EntityModel<LivingEntityRenderState>) model).setupAnim(state);
    }
}
*///?}
