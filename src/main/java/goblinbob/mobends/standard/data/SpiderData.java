package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.core.ModStatics;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

public class SpiderData extends LivingEntityData<EntitySpider>
{

    /** The spider's leg geometry, in model units (shared by the mutator and the leg drivers). */
    public static final int LIMBS = 8;
    /** Length of each of the two segments of a leg. */
    public static final float LEG_SEGMENT_LENGTH = 12F;
    /** Where the hips are: to either side of the body, and at this height. */
    public static final float HIP_X = 4F;
    public static final float HIP_Y = 15F;
    /** Where the knee is along the upper segment. */
    public static final float KNEE_X = 11F;
    public static final float KNEE_Y = -1F;

    public ModelPartTransform spiderHead;
    public ModelPartTransform spiderNeck;
    public ModelPartTransform spiderBody;

    public Limb[] limbs;

    private static final ResourceLocation ANIMATOR = new ResourceLocation(ModStatics.MODID, "bends/animators/spider.json");

    protected float prevCrawlProgress = 0;
    protected float crawlProgress = 0;
    protected EnumFacing wallFacing = null;

    public SpiderData(EntitySpider entity)
    {
        super(entity);
    }

    @Override
    protected ResourceLocation getDefaultAnimator()
    {
        return ANIMATOR;
    }

    @Override
    protected void registerKumoBindings()
    {
        super.registerKumoBindings();
        registerVariable("crawlProgress", this::getInterpolatedCrawlProgress);
        registerVariable("crawlRenderYaw", () -> {
            final float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * DataUpdateHandler.partialTicks;
            return MathHelper.wrapDegrees(yaw - getCrawlingRotation());
        });
        registerState("BESIDE_CLIMBABLE", entity::isBesideClimbableBlock);
    }

    public float getInterpolatedCrawlProgress()
    {
        return GUtil.lerp(prevCrawlProgress, crawlProgress, DataUpdateHandler.partialTicks);
    }

    @Override
    public void initModelPose()
    {
        super.initModelPose();

        this.spiderBody = new ModelPartTransform();
        this.spiderNeck = new ModelPartTransform();
        this.spiderHead = new ModelPartTransform();
        this.limbs = new Limb[LIMBS];

        for (int i = 0; i < limbs.length; ++i)
        {
            limbs[i] = new Limb(this, i);
            nameToPartMap.put("leg" + (i + 1), limbs[i].upperPart);
            nameToPartMap.put("foreLeg" + (i + 1), limbs[i].lowerPart);
        }

        nameToPartMap.put("body", spiderBody);
        nameToPartMap.put("neck", spiderNeck);
        nameToPartMap.put("head", spiderHead);

        this.spiderHead.position.set(0.0F, 15.0F, -3.0F);
        this.spiderNeck.position.set(0.0F, 15.0F, 0.0F);
        this.spiderBody.position.set(0.0F, 15.0F, 9.0F);
    }

    @Override
    public void updateParts(float ticksPerFrame)
    {
        super.updateParts(ticksPerFrame);

        this.spiderBody.update(ticksPerFrame);
        this.spiderNeck.update(ticksPerFrame);
        this.spiderHead.update(ticksPerFrame);

        for (Limb limb : limbs)
        {
            limb.upperPart.update(ticksPerFrame);
            limb.lowerPart.update(ticksPerFrame);
        }
    }

    @Override
    public void updateClient()
    {
        super.updateClient();

        for (Limb limb : limbs)
        {
            limb.updateClient();
        }

        prevCrawlProgress = crawlProgress;
        crawlProgress += MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        wallFacing = calcWallFacing();
    }

    public EnumFacing calcWallFacing()
    {
        if (!entity.isOnLadder())
        {
            return null;
        }

        final BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));

        final IBlockState blockN = entity.world.getBlockState(position.add(EnumFacing.NORTH.getDirectionVec()));
        final IBlockState blockS = entity.world.getBlockState(position.add(EnumFacing.SOUTH.getDirectionVec()));
        final IBlockState blockW = entity.world.getBlockState(position.add(EnumFacing.WEST.getDirectionVec()));
        final IBlockState blockE = entity.world.getBlockState(position.add(EnumFacing.EAST.getDirectionVec()));

        if (blockN.getBlock() != Blocks.AIR) return EnumFacing.NORTH;
        if (blockS.getBlock() != Blocks.AIR) return EnumFacing.SOUTH;
        if (blockW.getBlock() != Blocks.AIR) return EnumFacing.WEST;
        if (blockE.getBlock() != Blocks.AIR) return EnumFacing.EAST;
        return null;
    }

    public float getCrawlingRotation()
    {
        if (wallFacing == null)
            return 0.0F;

        return wallFacing.getHorizontalAngle();
    }

    public static class Limb
    {

        private final SpiderData data;
        public final ModelPartTransform upperPart;
        public final ModelPartTransform lowerPart;
        private final int index;
        private final boolean odd;
        private final double neutralYaw;

        private double worldX, worldZ, prevWorldX, prevWorldZ;
        private  double adjustTargetX = 0;
        private double adjustTargetZ = 0;
        private float adjustingProgress = 1F;
        private float adjustingSpeed = 0.2F;

        public Limb(SpiderData data, int index)
        {
            this.data = data;
            this.upperPart = new ModelPartTransform();
            this.lowerPart = new ModelPartTransform();
            this.index = index;
            this.odd = index % 2 == 1;

            double neutralYaw = (double) this.index / (LIMBS - 1) * 2 - 1;
            this.neutralYaw = this.odd ? (neutralYaw * 1.3) : (Math.PI - neutralYaw * 1.3);

            this.upperPart.position.set(odd ? HIP_X : -HIP_X, HIP_Y, hipZ(index));
            this.lowerPart.position.set(odd ? KNEE_X : -KNEE_X, KNEE_Y, 0F);
            this.resetPosition();
        }

        public void resetPosition()
        {
            final float distance = 1;
            final float bodyYaw = data.entity.renderYawOffset / 180F * GUtil.PI;

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
            setLocalPosition(MathHelper.cos(angle) * distance + this.upperPart.position.x * 0.0625F, MathHelper.sin(angle) * distance - this.upperPart.position.z * 0.0625F);
        }

        public void adjustToNeutralPosition()
        {
            if (adjustingProgress != 1)
                return;

            this.adjustingSpeed = 0.2F;
            this.adjustingProgress = 0;

            final float distance = 1.2F;
            final float bodyYaw = data.entity.renderYawOffset / 180F * GUtil.PI;
            this.adjustTargetX = Math.cos(this.neutralYaw + bodyYaw) * distance + data.getPositionX();
            this.adjustTargetZ = Math.sin(this.neutralYaw + bodyYaw) * distance + data.getPositionZ();
        }

        public void adjustToLocalPosition(double x, double z, float adjustingSpeed)
        {
            if (this.adjustingProgress != 1)
                return;

            this.adjustingSpeed = adjustingSpeed;
            this.adjustingProgress = 0;
            final float bodyYaw = data.entity.renderYawOffset / 180F * GUtil.PI;
            this.adjustTargetX = x * Math.cos(bodyYaw) - z * Math.sin(bodyYaw) + data.getPositionX();
            this.adjustTargetZ = x * Math.sin(bodyYaw) + z * Math.cos(bodyYaw) + data.getPositionZ();
        }

        public void setLocalPosition(double x, double z)
        {
            this.adjustingProgress = 1;
            final float bodyYaw = data.entity.renderYawOffset / 180F * GUtil.PI;
            this.worldX = this.adjustTargetX = x * Math.cos(bodyYaw) - z * Math.sin(bodyYaw) + data.getPositionX();
            this.worldZ = this.adjustTargetZ = x * Math.sin(bodyYaw) + z * Math.cos(bodyYaw) + data.getPositionZ();
        }

        /** Where the foot is relative to the hip, the body offset by {@code bodyX}, {@code bodyZ}; written into {@code result}. */
        public void solveIK(double bodyX, double bodyZ, float pt, IKResult result)
        {
            final double renderYawOffset = (data.entity.prevRenderYawOffset + (data.entity.renderYawOffset - data.entity.prevRenderYawOffset) * pt) / 180F * Math.PI;
            final double spiderX = data.entity.prevPosX + (data.entity.posX - data.entity.prevPosX) * pt;
            final double spiderZ = data.entity.prevPosZ + (data.entity.posZ - data.entity.prevPosZ) * pt;
            final double worldLimbX = this.prevWorldX + (this.worldX - this.prevWorldX) * pt;
            final double worldLimbZ = this.prevWorldZ + (this.worldZ - this.prevWorldZ) * pt;
            final double x = (worldLimbX - spiderX) / 0.0625;
            final double z = -(worldLimbZ - spiderZ) / 0.0625;
            final double localX = x * Math.cos(renderYawOffset) - z * Math.sin(renderYawOffset) - bodyX;
            final double localZ = x * Math.sin(renderYawOffset) + z * Math.cos(renderYawOffset) - bodyZ;
            final double deltaX = (this.upperPart.position.x - localX);
            final double deltaZ = (this.upperPart.position.z - localZ);
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