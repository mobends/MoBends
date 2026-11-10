package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.data.RemoteFlight;
import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.ItemEffects;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.math.Quaternion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * The player's cape ({@code {"mobends:cape": {...}}}): vanilla's cloak physics (CapeLayer, from the
 * cloak position that trails the player), written as the cape bone's rotation, for any player. It
 * follows over {@link #DEFAULT_SMOOTHING} ticks unless the item's {@code @smoothing} says otherwise.
 */
public class CapeDriver implements IPoseItem {
    public static final float DEFAULT_SMOOTHING = 2F;

    private final int slot;
    private final int[] written;
    private final ItemEffects effects;
    private final Quaternion rotation = new Quaternion();
    private final Quaternion temp = new Quaternion();

    public CapeDriver(int slot, ItemEffects effects) {
        this.slot = slot;
        this.written = new int[] { slot };
        this.effects = effects;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, Template template) throws MalformedKumoTemplateException {
        ItemEffects effects = new ItemEffects(context.getExpressionScope(), skeleton, template.smoothing, template.vectorModes, template.snap);
        return new CapeDriver(skeleton.indexOf(template.bone == null ? "cape" : template.bone), effects.isEmpty() ? null : effects);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException {
        if (!(context.getSubject() instanceof LivingEntityData<?> data) || !(data.getEntity() instanceof Player player)) {
            return;
        }

        // As CapeLayer computes it, in 1.21.1.
        float partialTicks = DataUpdateHandler.partialTicks;
        //? if >=26.1 {
        /*if (!(player instanceof net.minecraft.client.entity.ClientAvatarEntity avatar)) {
            return;
        }
        double d0 = avatar.avatarState().getInterpolatedCloakX(partialTicks) - Mth.lerp(partialTicks, player.xo, player.getX());
        double d1 = avatar.avatarState().getInterpolatedCloakY(partialTicks) - Mth.lerp(partialTicks, player.yo, player.getY());
        double d2 = avatar.avatarState().getInterpolatedCloakZ(partialTicks) - Mth.lerp(partialTicks, player.zo, player.getZ());
        *///?} else {
        double d0 = Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX());
        double d1 = Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY());
        double d2 = Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ());
        //?}
        float f = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        double d3 = Mth.sin(f * Mth.DEG_TO_RAD);
        double d4 = -Mth.cos(f * Mth.DEG_TO_RAD);
        float f1 = Mth.clamp((float) d1 * 10.0F, -6.0F, 32.0F);
        float f2 = Mth.clamp((float) (d0 * d3 + d2 * d4) * 100.0F, 0.0F, 150.0F);
        float f3 = Mth.clamp((float) (d0 * d4 - d2 * d3) * 100.0F, -20.0F, 20.0F);
        //? if >=26.1 {
        /*float f4 = avatar.avatarState().getInterpolatedBob(partialTicks);
        f1 += Mth.sin(avatar.avatarState().getInterpolatedWalkDistance(partialTicks) * 6.0F) * 32.0F * f4;
        *///?} else {
        float f4 = Mth.lerp(partialTicks, player.oBob, player.bob);
        f1 += Mth.sin(Mth.lerp(partialTicks, player.walkDistO, player.walkDist) * 6.0F) * 32.0F * f4;
        //?}
        if (player.isCrouching()) {
            f1 += 25.0F;
        }

        if (RemoteFlight.isFlying(player) && player.isSprinting()) {
            // Streams straight back.
            rotation.setIdentity();
        } else {
            // Lifted about X, then swayed about Z and Y by the sideways motion. (Vanilla turns its
            // cloak a further 180 degrees about Y, as its cloak faces the other way.)
            PoseMath.axisAngleDegrees(1, 0, 0, 6.0F + f2 / 2.0F + f1, rotation);
            PoseMath.axisAngleDegrees(0, 0, 1, f3 / 2.0F, temp);
            Quaternion.mul(temp, rotation, rotation);
            PoseMath.axisAngleDegrees(0, 1, 0, -f3 / 2.0F, temp);
            Quaternion.mul(temp, rotation, rotation);
        }

        pose.composeRotation(slot, rotation, Pose.Space.OVERRIDE);
        pose.get(slot).smoothing = DEFAULT_SMOOTHING;
        if (effects != null) {
            effects.apply(pose, written, context);
        }
    }

    @Override
    public void onNodeStarted(IKumoContext context) {
    }

    @Override
    public void advance(IKumoContext context, float deltaTime) {
    }

    public static class Template extends DriverItemTemplate {
        /** The cape's bone; null for "cape". */
        public String bone;
    }
}
