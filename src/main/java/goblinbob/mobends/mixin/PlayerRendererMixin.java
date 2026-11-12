package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.client.definition.DefinedRenderer;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.standard.client.renderer.entity.layers.PlayerAccessoriesLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/^*
 * Gives the player's renderer (26.x's {@code AvatarRenderer}) Mo' Bends' layer of supporter
 * accessories ({@link PlayerAccessoriesLayer}), which draws nothing for a player drawn as vanilla.
 * (It extends the renderer's superclass to reach {@code addLayer}, which isn't public.)
 * <p>
 * A bent player who swims or crawls isn't tilted by its renderer: its definition lays the body down
 * itself ({@link DefinedRenderer#replacesSwimmingTilt}).
 ^/
@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {
    private PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mobends$addAccessoriesLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        addLayer(new PlayerAccessoriesLayer(this));
    }

    @WrapOperation(
        method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;swimAmount:F")
    )
    private float mobends$noSwimTiltWhenBent(AvatarRenderState state, Operation<Float> original) {
        DefinedEntityData<?> bent = ((BentRenderState) state).mobends$bent();
        if (bent != null && DefinedRenderer.replacesSwimmingTilt(bent)) {
            return 0.0F;
        }
        return original.call(state);
    }
}
*///?} else {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import goblinbob.mobends.core.client.BendingEntityRenderer;
import goblinbob.mobends.core.client.definition.DefinedRenderer;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.standard.client.renderer.entity.layers.PlayerAccessoriesLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives the player's renderer Mo' Bends' layer of supporter accessories ({@link PlayerAccessoriesLayer}),
 * which 1.12 added for the definitions that ask for it; it draws nothing for a player drawn as
 * vanilla. (It extends the renderer's superclass to reach {@code addLayer}, which isn't public on
 * every loader.)
 * <p>
 * Keeps vanilla's swimming tilt off a bent player in water. Vanilla turns a swimming player's whole
 * model face down in {@code setupRotations}; the player animator turns the root bone for its own
 * swimming poses (1.12 had no swimming pose), and the bent model is drawn inside vanilla's turn, so
 * both would apply and a player sprint-swimming underwater would end up nearly upside down.
 * Out of water (crawling through a one-block gap) vanilla's tilt stays: the animator has no
 * crawling pose, and the tilt keeps the player inside the gap.
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mobends$addAccessoriesLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        addLayer(new PlayerAccessoriesLayer(this));
    }

    @WrapOperation(
        //? if >=1.21 {
        method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
        //?} else {
        /*method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",
        *///?}
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getSwimAmount(F)F")
    )
    private float mobends$noSwimTiltWhenBent(AbstractClientPlayer player, float partialTicks, Operation<Float> original) {
        DefinedEntityData<?> bent = ((BendingEntityRenderer) this).mobends$bentData();
        if (bent != null && DefinedRenderer.replacesSwimmingTilt(bent)) {
            return 0.0F;
        }
        return original.call(player, partialTicks);
    }
}
//?}
