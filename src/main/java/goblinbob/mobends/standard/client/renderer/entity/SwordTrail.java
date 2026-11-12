package goblinbob.mobends.standard.client.renderer.entity;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityComponent;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.core.util.IColorRead;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * {@code mobends:sword_trail}: the streak a swung sword leaves, fed by the {@code mobends:sword_trail}
 * driver and drawn in the entity's frame.
 */
public class SwordTrail implements EntityComponent
{
    protected final Supplier<IColorRead> baseColor;
    protected LinkedList<TrailPart> trailPartList = new LinkedList<>();

    public SwordTrail(Supplier<IColorRead> baseColor)
    {
        this.baseColor = baseColor;
    }

    public void reset()
    {
        trailPartList.clear();
    }

    protected static class TrailPart
    {
        protected EnumHandSide primaryHand;
        protected IColorRead baseColor;

        protected ModelPartTransform body;
        protected ModelPartTransform arm;
        protected ModelPartTransform foreArm;

        // The rotations the renderer applies itself, turned into the trail's frame (see #inTrailFrame).
        /** Vanilla's elytra flight: the turn towards where the player moves, then the pitch (RenderPlayer#applyRotations). */
        protected Quaternion elytraTurn = new Quaternion();
        protected Quaternion elytraPitch = new Quaternion();
        /** The entity's own bones (see MutatedRenderer#beforeRender): their rotations and positions. */
        protected Quaternion rootRotation = new Quaternion();
        protected Vec3f rootPosition = new Vec3f();
        protected Quaternion centerRotation = new Quaternion();
        protected Vec3f centerPosition = new Vec3f();
        /** Where the center bone turns about, in pixels. */
        protected Vec3f centerPivot = new Vec3f();
        protected Quaternion itemRotation = new Quaternion();
        protected Vec3f position = new Vec3f();

        protected float velocityX, velocityY, velocityZ;
        protected float ticksExisted = 0F;

        public TrailPart(EnumHandSide primaryHand, IColorRead baseColor, float velocityX, float velocityY, float velocityZ)
        {
            this.body = new ModelPartTransform();
            this.arm = new ModelPartTransform();
            this.foreArm = new ModelPartTransform();
            this.primaryHand = primaryHand;
            this.baseColor = baseColor;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.velocityZ = velocityZ;
        }

        public void update(float ticksPerFrame)
        {
            this.ticksExisted += ticksPerFrame;
            this.position.x += this.velocityX * ticksPerFrame;
            this.position.y += this.velocityY * ticksPerFrame;
            this.position.z += this.velocityZ * ticksPerFrame;
        }

        public void draw(boolean first, boolean last)
        {
            float alpha = ticksExisted / 5F;
            alpha = Math.min(alpha, 1F);
            alpha = 1F - alpha;

            final Vec3f[] points = new Vec3f[] {
                    new Vec3f(0, 0, -8 + 8 * alpha),
                    new Vec3f(0, 0, -8 - 8 * alpha)
            };

            GUtil.translate(points, 0, 0, 16);
            GUtil.rotate(points, itemRotation);
            GUtil.translate(points, primaryHand == EnumHandSide.LEFT ? 1 : -1, -6, 0);
            GUtil.rotate(points, foreArm.rotation.getSmooth());
            GUtil.translate(points, 0, -6 + 2, 0);
            GUtil.rotate(points, arm.rotation.getSmooth());
            GUtil.translate(points, arm.position.x, 10, 0);
            GUtil.rotate(points, body.rotation.getSmooth());
            GUtil.translate(points, 0, 12, 0);
            // The rotations of the whole model, in the order the renderer applies them: vanilla's
            // (about the feet), then those of MutatedRenderer#beforeRender.
            GUtil.rotate(points, elytraTurn);
            GUtil.rotate(points, elytraPitch);
            GUtil.translate(points, -centerPivot.x, -centerPivot.y, -centerPivot.z);
            GUtil.rotate(points, centerRotation);
            GUtil.translate(points, centerPivot.x, centerPivot.y, centerPivot.z);
            GUtil.translate(points, centerPosition.x, centerPosition.y, centerPosition.z);
            GUtil.rotate(points, rootRotation);
            GUtil.translate(points, rootPosition.x, rootPosition.y, rootPosition.z);

            for (final Vec3f point : points)
            {
                point.add(position);
            }

            GlStateManager.color(baseColor.getR(), baseColor.getG(), baseColor.getB(), alpha);

            if (!first)
            {
                // Closing up the previous strand
                GlStateManager.glVertex3f(points[1].x, points[1].y, points[1].z);
                GlStateManager.glVertex3f(points[0].x, points[0].y, points[0].z);
            }

            GlStateManager.glVertex3f(points[0].x, points[0].y, points[0].z);
            GlStateManager.glVertex3f(points[1].x, points[1].y, points[1].z);

            if (last)
            {
                // Closing the trail
                GlStateManager.glVertex3f(points[1].x, points[1].y, points[1].z);
                GlStateManager.glVertex3f(points[0].x, points[0].y, points[0].z);
            }
        }
    }

    public void render()
    {
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.pushMatrix();

        GlStateManager.glBegin(GL11.GL_QUADS);

        Iterator<TrailPart> it = trailPartList.iterator();
        boolean first = true;
        while (it.hasNext())
		{
			final TrailPart part = it.next();
			part.draw(first, !it.hasNext());
			first = false;
		}
        GlStateManager.glEnd();

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
    }

    /**
     * Notes the arm holding the main hand's item where it is now ({@code body}, then
     * {@code rightArm}, {@code rightForeArm} and {@code rightHeldItem}, or the left ones), moving
     * by the velocity as it fades. An entity without those parts leaves no trail.
     */
    public void add(LivingEntityData<?> entityData, float velocityX, float velocityY, float velocityZ)
    {
        final EntityLivingBase entity = entityData.getEntity();
        final EnumHandSide primaryHand = entity.getPrimaryHand();
        final String side = primaryHand == EnumHandSide.RIGHT ? "right" : "left";
        final Object body = entityData.getPartForName("body");
        final Object arm = entityData.getPartForName(side + "Arm");
        final Object foreArm = entityData.getPartForName(side + "ForeArm");
        final Object item = entityData.getPartForName(side + "HeldItem");
        if (!(body instanceof IModelPart) || !(arm instanceof IModelPart) || !(foreArm instanceof IModelPart))
        {
            return;
        }

        final TrailPart newPart = new TrailPart(primaryHand, this.baseColor.get(), velocityX, velocityY, velocityZ);
        newPart.body.syncUp((IModelPart) body);
        newPart.arm.syncUp((IModelPart) arm);
        newPart.foreArm.syncUp((IModelPart) foreArm);
        if (item instanceof SmoothOrientation)
        {
            newPart.itemRotation.set(((SmoothOrientation) item).getSmooth());
        }
        else
        {
            newPart.itemRotation.setIdentity();
        }

        newPart.rootPosition.set(entityData.root.position.getX(),
                entityData.root.position.getY(),
                entityData.root.position.getZ());
        inTrailFrame(entityData.root.rotation.getSmooth(), newPart.rootRotation);
        newPart.centerPosition.set(entityData.center.position.getX(),
                entityData.center.position.getY(),
                entityData.center.position.getZ());
        inTrailFrame(entityData.center.rotation.getSmooth(), newPart.centerRotation);
        if (entity instanceof AbstractClientPlayer && entity.isElytraFlying())
        {
            setElytraRotations((AbstractClientPlayer) entity, DataUpdateHandler.partialTicks, newPart);
        }
        Vec3f pivot = entityData.getCenterPivot(newPart.centerPivot);
        pivot.set(pivot.x * 16, pivot.y * 16, pivot.z * 16);

        trailPartList.add(newPart);
    }

    /**
     * A rotation the renderer applies with GL in the entity's frame, as {@link GUtil#rotate} takes it
     * to turn the trail the same way. The trail is drawn in that frame, which is the model's with Y
     * and Z negated, and GUtil#rotate takes rotations of the model's frame (the bones'), turning
     * (x, y, z, w) into (x, -y, -z, w); so a rotation of the entity's frame goes in as (x, -y, -z, w).
     */
    private static void inTrailFrame(Quaternion glRotation, Quaternion dest)
    {
        dest.set(glRotation.x, -glRotation.y, -glRotation.z, glRotation.w);
    }

    /** A rotation by {@code degrees} about an axis, as GL applies it, in the trail's frame. */
    private static void inTrailFrame(float degrees, float x, float y, float z, Quaternion dest)
    {
        final double half = Math.toRadians(degrees) / 2;
        final float sin = (float) Math.sin(half);
        dest.set(x * sin, -y * sin, -z * sin, (float) Math.cos(half));
    }

    /**
     * Vanilla's rotations of a player flying with an elytra, as RenderPlayer#applyRotations applies
     * them after its turn by 180 - yaw. The trail's frame faces the other way (MutatedRenderer turns
     * by -yaw), which flips the pitch and leaves the turn about Y as it is.
     */
    private static void setElytraRotations(AbstractClientPlayer player, float partialTicks, TrailPart part)
    {
        final float ticks = player.getTicksElytraFlying() + partialTicks;
        final float pitch = MathHelper.clamp(ticks * ticks / 100F, 0F, 1F) * (-90F - player.rotationPitch);
        inTrailFrame(-pitch, 1, 0, 0, part.elytraPitch);

        final Vec3 look = player.getLook(partialTicks);
        final double motion = player.motionX * player.motionX + player.motionZ * player.motionZ;
        final double lookXZ = look.x * look.x + look.z * look.z;
        if (motion > 0 && lookXZ > 0)
        {
            final double cos = (player.motionX * look.x + player.motionZ * look.z) / (Math.sqrt(motion) * Math.sqrt(lookXZ));
            final double cross = player.motionX * look.z - player.motionZ * look.x;
            inTrailFrame((float) (Math.signum(cross) * Math.acos(cos) * 180 / Math.PI), 0, 1, 0, part.elytraTurn);
        }
    }

    public void add(LivingEntityData<?> entityData)
    {
        add(entityData, 0, 0, 0);
    }

    @Override
    public void renderLocal(float scale)
    {
        if (!ModConfig.showSwordTrail)
        {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        render();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    @Override
	public void update(float ticksPerFrame)
    {
    	final Iterator<TrailPart> it = trailPartList.iterator();
        while (it.hasNext())
        {
            final TrailPart trailPart = it.next();
            trailPart.update(ticksPerFrame);

            if (trailPart.ticksExisted > 20)
			{
				it.remove();
			}
        }
    }
}
