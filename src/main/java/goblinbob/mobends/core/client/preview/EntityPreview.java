package goblinbob.mobends.core.client.preview;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.bender.EntityBender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;

/**
 * A bender's mob, animated in a frame of the Settings window: an entity of the bender's class (the
 * player's own copy for players) standing in a {@link PreviewWorld}, drawn orthographically from
 * three quarters above and fit to the frame by its hitbox. It idles, or walks in place while
 * {@link #tick(boolean) asked to}. A mob that can't be made or drawn shows its spawn egg instead.
 */
public class EntityPreview
{

    /** How far the mob is turned from facing the viewer, and how far the view looks down at it. */
    private static final float YAW = 35F;
    private static final float TILT = 20F;
    /** Blocks per tick, about a mob's walking pace. */
    private static final double WALK_SPEED = 0.2D;

    @Nullable
    private final PreviewWorld world;
    private final EntityBender<?> bender;

    private boolean made;
    @Nullable
    private EntityLivingBase entity;
    /** Drawn when there is no entity: the mob's spawn egg, or nothing (a question mark) if it has none. */
    @Nullable
    private ItemStack fallbackIcon;

    /** @param world Where the mob stands; null to always show the fallback. */
    public EntityPreview(@Nullable PreviewWorld world, EntityBender<?> bender)
    {
        this.world = world;
        this.bender = bender;
    }

    /** Advances the mob by a tick: walking forward if {@code walking}, else standing still. */
    public void tick(boolean walking)
    {
        if (entity == null)
        {
            return;
        }

        entity.ticksExisted++;
        entity.prevPosX = entity.lastTickPosX = entity.posX;
        entity.prevPosY = entity.lastTickPosY = entity.posY;
        entity.prevPosZ = entity.lastTickPosZ = entity.posZ;

        // Forward is where the mob faces (yaw 0: +z), as the animators tell from the motion.
        final double speed = walking ? WALK_SPEED : 0D;
        entity.setPosition(entity.posX, entity.posY, entity.posZ + speed);
        entity.onGround = true;

        // As EntityLivingBase#onLivingUpdate swings the limbs.
        entity.prevLimbSwingAmount = entity.limbSwingAmount;
        final float swing = Math.min((float) speed * 4F, 1F);
        entity.limbSwingAmount += (swing - entity.limbSwingAmount) * 0.4F;
        entity.limbSwing += entity.limbSwingAmount;

        if (entity instanceof EntityPlayer)
        {
            // The cape follows these; left behind, it would fly up.
            final EntityPlayer player = (EntityPlayer) entity;
            player.prevChasingPosX = player.chasingPosX = player.posX;
            player.prevChasingPosY = player.chasingPosY = player.posY;
            player.prevChasingPosZ = player.chasingPosZ = player.posZ;
        }
    }

    /**
     * Draws the mob centered on the origin, fit in a square {@code size} wide. Clears the depth
     * buffer before and after, so it is to be drawn clipped to its frame (see UIScissorHelper).
     */
    public void draw(int size, float partialTicks)
    {
        if (!made)
        {
            made = true;
            entity = world != null ? make(world) : null;
            if (entity == null)
            {
                fallbackIcon = spawnEggOf(bender.entityClass);
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        if (entity != null)
        {
            final int matrixDepth = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
            try
            {
                drawEntity(entity, size, partialTicks);
            }
            catch (Throwable e)
            {
                Core.LOG.warning("Unable to draw the preview of " + bender.getKey() + ": " + e);
                world.remove(entity);
                entity = null;
                fallbackIcon = spawnEggOf(bender.entityClass);
                // The render stopped halfway: what it pushed is left on the stack.
                while (GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) > matrixDepth)
                {
                    GlStateManager.popMatrix();
                }
                finishEntityDraw();
            }
        }
        else
        {
            drawFallback(size);
        }
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
    }

    private static void drawEntity(EntityLivingBase entity, int size, float partialTicks)
    {
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        // The hitbox's outline from the view, an upright box turned by YAW and seen from TILT above.
        final float spread = Math.abs(MathHelper.cos(YAW * 0.017453292F)) + Math.abs(MathHelper.sin(YAW * 0.017453292F));
        final float width = entity.width * spread;
        final float height = entity.height * MathHelper.cos(TILT * 0.017453292F) + width * MathHelper.sin(TILT * 0.017453292F);
        final float scale = size / Math.max(Math.max(width, height), 0.1F);

        GlStateManager.enableDepth();
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        // In front of the frame, and up the right way (as GuiInventory#drawEntityOnScreen).
        GlStateManager.translate(0F, 0F, 100F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180F, 0F, 0F, 1F);
        GlStateManager.rotate(135F, 0F, 1F, 0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135F, 0F, 1F, 0F);
        GlStateManager.rotate(TILT, 1F, 0F, 0F);
        GlStateManager.rotate(YAW, 0F, 1F, 0F);
        // Turned about the hitbox's center, which lands on the origin.
        GlStateManager.translate(0F, -entity.height / 2F, 0F);

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        renderManager.setPlayerViewY(180F);
        renderManager.setRenderShadow(false);
        try
        {
            renderManager.renderEntity(entity, 0D, 0D, 0D, 0F, partialTicks, false);
        }
        finally
        {
            renderManager.setRenderShadow(true);
        }

        GlStateManager.popMatrix();
        finishEntityDraw();
    }

    private static void finishEntityDraw()
    {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private void drawFallback(int size)
    {
        final Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        if (fallbackIcon != null)
        {
            final float scale = size / 16F;
            GlStateManager.scale(scale, scale, 1F);
            GlStateManager.translate(-8F, -8F, 0F);
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(fallbackIcon, 0, 0);
            RenderHelper.disableStandardItemLighting();
        }
        else
        {
            GlStateManager.scale(2F, 2F, 1F);
            mc.fontRenderer.drawStringWithShadow("?", -mc.fontRenderer.getStringWidth("?") / 2F + 0.5F, -3.5F, 0x808080);
        }
        GlStateManager.popMatrix();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    /** The entity shown, registered with the preview world; null if one can't be made. */
    @Nullable
    private EntityLivingBase make(PreviewWorld world)
    {
        try
        {
            final EntityLivingBase made = construct(world, bender.entityClass);
            if (made == null)
            {
                Core.LOG.warning("Unable to preview " + bender.getKey() + ": no entity of " + bender.entityClass.getName() + " can be made.");
                return null;
            }
            world.register(made, bender);
            return made;
        }
        catch (Throwable e)
        {
            Core.LOG.warning("Unable to preview " + bender.getKey() + ": " + e);
            return null;
        }
    }

    @Nullable
    private static EntityLivingBase construct(PreviewWorld world, Class<? extends EntityLivingBase> entityClass) throws ReflectiveOperationException
    {
        if (EntityPlayer.class.isAssignableFrom(entityClass) && entityClass.isAssignableFrom(EntityOtherPlayerMP.class))
        {
            final EntityPlayer player = Minecraft.getMinecraft().player;
            return player == null ? null : new PreviewPlayer(world, player);
        }

        final Class<? extends EntityLivingBase> concrete = concreteClassOf(entityClass);
        return concrete == null ? null : concrete.getConstructor(World.class).newInstance(world);
    }

    /**
     * The class itself if entities of it can be made, else the first registered entity class
     * extending it. Null if there is none.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends EntityLivingBase> Class<? extends E> concreteClassOf(Class<E> entityClass)
    {
        if (!Modifier.isAbstract(entityClass.getModifiers()) && !entityClass.isInterface())
        {
            return entityClass;
        }
        for (EntityEntry entry : ForgeRegistries.ENTITIES.getValuesCollection())
        {
            final Class<? extends Entity> registered = entry.getEntityClass();
            if (entityClass.isAssignableFrom(registered) && !Modifier.isAbstract(registered.getModifiers()))
            {
                return (Class<? extends E>) registered;
            }
        }
        return null;
    }

    @Nullable
    private static ItemStack spawnEggOf(Class<? extends EntityLivingBase> entityClass)
    {
        final Class<? extends EntityLivingBase> concrete = concreteClassOf(entityClass);
        final ResourceLocation id = concrete == null ? null : EntityList.getKey(concrete);
        if (id == null || !EntityList.ENTITY_EGGS.containsKey(id))
        {
            return null;
        }
        final ItemStack egg = new ItemStack(Items.SPAWN_EGG);
        ItemMonsterPlacer.applyEntityIdToItemStack(egg, id);
        return egg;
    }

    /** A copy of the player: their skin, the parts of it they show, their hand and what they hold and wear. */
    private static class PreviewPlayer extends EntityOtherPlayerMP
    {

        PreviewPlayer(World world, EntityPlayer source)
        {
            super(world, source.getGameProfile());
            this.dataManager.set(PLAYER_MODEL_FLAG, source.getDataManager().get(PLAYER_MODEL_FLAG));
            this.setPrimaryHand(source.getPrimaryHand());
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
            {
                this.setItemStackToSlot(slot, source.getItemStackFromSlot(slot).copy());
            }
        }

    }

}
