package goblinbob.mobends.standard.kumo.spider;

import goblinbob.mobends.core.data.EntityComponent;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.util.math.MathHelper;

/**
 * {@code mobends:spider_legs}: where a spider's feet are planted in the world, which its leg
 * drivers ({@code mobends:spider_idle_legs}, {@code mobends:spider_moving_legs}) read and move.
 * Both drivers share it, so a foot stays where one left it when the other takes over.
 */
public class SpiderLegs implements EntityComponent
{

    /** The spider's leg geometry, in model units (the legs' bones are placed and sized so). */
    public static final int LIMBS = 8;
    /** Length of each of the two segments of a leg. */
    public static final float LEG_SEGMENT_LENGTH = 12F;
    /** Where the hips are: to either side of the body, and at this height. */
    public static final float HIP_X = 4F;
    public static final float HIP_Y = 15F;
    /** Where the knee is along the upper segment. */
    public static final float KNEE_X = 11F;
    public static final float KNEE_Y = -1F;

    public final Limb[] limbs = new Limb[LIMBS];

    public SpiderLegs(LivingEntityData<?> data)
    {
        for (int i = 0; i < LIMBS; i++)
        {
            limbs[i] = new Limb(data, i);
        }
    }

    @Override
    public void update(float ticksPerFrame)
    {
    }

    @Override
    public void updateClient()
    {
        for (Limb limb : limbs)
        {
            limb.updateClient();
        }
    }

    public static class Limb
    {

        private final LivingEntityData<?> data;
        /** Where the hip is, in model units (as the leg bones are placed). */
        private final float hipX, hipZ;
        private final int index;
        private final boolean odd;
        private final double neutralYaw;

        private double worldX, worldZ, prevWorldX, prevWorldZ;
        private  double adjustTargetX = 0;
        private double adjustTargetZ = 0;
        private float adjustingProgress = 1F;
        private float adjustingSpeed = 0.2F;

        Limb(LivingEntityData<?> data, int index)
        {
            this.data = data;
            this.index = index;
            this.odd = index % 2 == 1;

            double neutralYaw = (double) this.index / (LIMBS - 1) * 2 - 1;
            this.neutralYaw = this.odd ? (neutralYaw * 1.3) : (Math.PI - neutralYaw * 1.3);

            this.hipX = odd ? HIP_X : -HIP_X;
            this.hipZ = hipZ(index);
            this.resetPosition();
        }

        public void resetPosition()
        {
            final float distance = 1;
            final float bodyYaw = data.getEntity().renderYawOffset / 180F * GUtil.PI;

            this.worldX = Math.cos(this.neutralYaw + bodyYaw) * distance + data.getPositionX();
            this.worldZ = Math.sin(this.neutralYaw + bodyYaw) * distance + data.getPositionZ();
            this.prevWorldX = this.worldX;
            this.prevWorldZ = this.worldZ;
        }

        void updateClient()
        {
            this.prevWorldX = this.worldX;
            this.prevWorldZ = this.worldZ;

            if (adjustingProgress < 1)
            {
                adjustingProgress += this.adjustingSpeed;
                if (adjustingProgress >= 1)
                {
                    this.worldX = this.adjustTargetX;
                    this.worldZ = this.adjustTargetZ;
                    adjustingProgress = 1;
                }
                else
                {
                    this.worldX += (this.adjustTargetX - this.worldX) * 0.2;
                    this.worldZ += (this.adjustTargetZ - this.worldZ) * 0.2;
                }
            }
        }

        public void setAngleAndDistance(float angle, float distance)
        {
            setLocalPosition(MathHelper.cos(angle) * distance + this.hipX * 0.0625F, MathHelper.sin(angle) * distance - this.hipZ * 0.0625F);
        }

        public void adjustToNeutralPosition()
        {
            if (adjustingProgress != 1)
                return;

            this.adjustingSpeed = 0.2F;
            this.adjustingProgress = 0;

            final float distance = 1.2F;
            final float bodyYaw = data.getEntity().renderYawOffset / 180F * GUtil.PI;
            this.adjustTargetX = Math.cos(this.neutralYaw + bodyYaw) * distance + data.getPositionX();
            this.adjustTargetZ = Math.sin(this.neutralYaw + bodyYaw) * distance + data.getPositionZ();
        }

        public void adjustToLocalPosition(double x, double z, float adjustingSpeed)
        {
            if (this.adjustingProgress != 1)
                return;

            this.adjustingSpeed = adjustingSpeed;
            this.adjustingProgress = 0;
            final float bodyYaw = data.getEntity().renderYawOffset / 180F * GUtil.PI;
            this.adjustTargetX = x * Math.cos(bodyYaw) - z * Math.sin(bodyYaw) + data.getPositionX();
            this.adjustTargetZ = x * Math.sin(bodyYaw) + z * Math.cos(bodyYaw) + data.getPositionZ();
        }

        public void setLocalPosition(double x, double z)
        {
            this.adjustingProgress = 1;
            final float bodyYaw = data.getEntity().renderYawOffset / 180F * GUtil.PI;
            this.worldX = this.adjustTargetX = x * Math.cos(bodyYaw) - z * Math.sin(bodyYaw) + data.getPositionX();
            this.worldZ = this.adjustTargetZ = x * Math.sin(bodyYaw) + z * Math.cos(bodyYaw) + data.getPositionZ();
        }

        /** Where the foot is relative to the hip, the body offset by {@code bodyX}, {@code bodyZ}; written into {@code result}. */
        public void solveIK(double bodyX, double bodyZ, float pt, IKResult result)
        {
            final double renderYawOffset = (data.getEntity().prevRenderYawOffset + (data.getEntity().renderYawOffset - data.getEntity().prevRenderYawOffset) * pt) / 180F * Math.PI;
            final double spiderX = data.getEntity().prevPosX + (data.getEntity().posX - data.getEntity().prevPosX) * pt;
            final double spiderZ = data.getEntity().prevPosZ + (data.getEntity().posZ - data.getEntity().prevPosZ) * pt;
            final double worldLimbX = this.prevWorldX + (this.worldX - this.prevWorldX) * pt;
            final double worldLimbZ = this.prevWorldZ + (this.worldZ - this.prevWorldZ) * pt;
            final double x = (worldLimbX - spiderX) / 0.0625;
            final double z = -(worldLimbZ - spiderZ) / 0.0625;
            final double localX = x * Math.cos(renderYawOffset) - z * Math.sin(renderYawOffset) - bodyX;
            final double localZ = x * Math.sin(renderYawOffset) + z * Math.cos(renderYawOffset) - bodyZ;
            final double deltaX = (this.hipX - localX);
            final double deltaZ = (this.hipZ - localZ);
            result.xzDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            result.xzAngle = Math.atan2(deltaX, deltaZ);
        }

        public double getNeutralYaw()
        {
            return this.neutralYaw;
        }

        public float getAdjustingProgress()
        {
            return this.adjustingProgress;
        }

        public boolean isOdd()
        {
            return this.odd;
        }

    }

    /** Where a leg's foot is: its horizontal distance from the hip and the angle to it. */
    public static class IKResult
    {

        public double xzDistance;
        public double xzAngle;

    }

    /** The Z of the hips of limb {@code index}: pairs from the front to the back. */
    public static float hipZ(int index)
    {
        return 2 - (index / 2);
    }

}
