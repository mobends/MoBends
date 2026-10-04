package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.expr.Expression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The built-ins every animated entity has, which {@code EntityData} and {@code LivingEntityData}
 * provide under these names: the time, and the {@code entity…} values that hold for every living
 * entity (see misc/kumo-format.md, *Entity built-ins*).
 */
public final class EntityBuiltIns
{

    public static final List<String> NUMBERS = Collections.unmodifiableList(Arrays.asList(
            "ticks", "partialTicks", "ticksPerFrame", "random",
            "entityId", "entityTicksExisted",
            "entityLimbSwing", "entityLimbSwingAmount", "entitySwingProgress", "entityHeadYaw", "entityHeadPitch",
            "entityHealth", "entityItemUseTicks", "entityItemUseTicksLeft", "entityTicksElytraFlying",
            "entityTicksInAir", "entityTicksAfterTouchdown", "entityTicksFalling", "entityTicksAfterAttack",
            "entityLedgeHeight", "entityClimbingRotation", "entityClimbingCycle",
            "entityClimbingRenderYaw", "entityClimbingBodyYaw", "entityClimbingHeadYaw",
            "entityMotionY", "entityPrevMotionY", "entityInterpolatedMotionY", "entitySpeed", "entityXZSpeed",
            "entityForwardMomentum", "entitySidewaysMomentum",
            "entityBodyYaw", "entityWorldX", "entityWorldY", "entityWorldZ",
            "entityRidingRelativeHeadYaw", "entityRidingRelativeYaw"));

    public static final List<String> BOOLEANS = Collections.unmodifiableList(Arrays.asList(
            "entityIsOnGround", "entityIsStandingStill", "entityIsSprinting", "entityIsSneaking", "entityIsStrafing",
            "entityIsInWater", "entityIsUnderwater", "entityIsRiding", "entityIsRidingLiving", "entityIsAlive",
            "entityIsChild", "entityIsLeftHanded", "entityIsSwinging", "entityIsSleeping", "entityIsElytraFlying",
            "entityIsClimbing", "entityIsDrawingBow"));

    private EntityBuiltIns()
    {
    }

    static void register()
    {
        for (String name : NUMBERS)
        {
            Expression.registerSubjectBuiltIn(name, Expression.Type.NUMBER);
        }
        for (String name : BOOLEANS)
        {
            Expression.registerSubjectBuiltIn(name, Expression.Type.BOOLEAN);
        }
    }

}
