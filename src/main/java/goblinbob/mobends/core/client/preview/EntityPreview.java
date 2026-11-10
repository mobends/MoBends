package goblinbob.mobends.core.client.preview;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.mixin.PlayerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A mob of the Mo' Bends menu, animated in a frame of the Settings window: an entity of the mob's
 * type (a copy of the player for the player) standing in a {@link PreviewWorld}, drawn
 * orthographically from three quarters above and fit to the frame by its hitbox, through the
 * entity's own renderer, so it's drawn bent as in the game. It idles, or walks in place while
 * {@link #tick(boolean) asked to}, and carries an arm swing through when made to {@link #swing()}.
 * A mob that can't be made or drawn shows its spawn egg instead.
 */
public class EntityPreview {
    /** How far the mob is turned from facing the viewer, and how far the view looks down at it. */
    private static final float YAW = 35F;
    private static final float TILT = 20F;
    /** Blocks per tick, about a mob's walking pace. */
    private static final double WALK_SPEED = 0.2D;
    /** The light it's drawn in: full, as the inventory draws the player. */
    private static final int LIGHT = 0xF000F0;

    @Nullable
    private final PreviewWorld world;
    private final DefinedEntity defined;
    /** Called with the entity once it is made, before it is first drawn. */
    private final Consumer<LivingEntity> setUp;

    private boolean made;
    @Nullable
    private LivingEntity entity;
    /** Where the entity stands when it isn't walking. */
    private Vec3 origin = Vec3.ZERO;
    /** Drawn when there is no entity: the mob's spawn egg, or nothing (a question mark) if it has none. */
    @Nullable
    private ItemStack fallbackIcon;

    /** @param world Where the mob stands; null to always show the fallback. */
    public EntityPreview(@Nullable PreviewWorld world, DefinedEntity defined) {
        this(world, defined, entity -> {});
    }

    /** @param setUp Called with the entity once it is made, before it is first drawn. */
    public EntityPreview(@Nullable PreviewWorld world, DefinedEntity defined, Consumer<LivingEntity> setUp) {
        this.world = world;
        this.defined = defined;
        this.setUp = setUp;
    }

    /** The entity shown, once it is made (on the first draw); null before, or if it can't be. */
    @Nullable
    public LivingEntity getEntity() {
        return entity;
    }

    /** Advances the mob by a tick: walking forward if {@code walking}, else standing still where it started. */
    public void tick(boolean walking) {
        if (entity == null) {
            return;
        }

        entity.tickCount++;
        if (!walking && !entity.position().equals(origin)) {
            // Back where it started, and its animation with it: walking carried its feet away.
            entity.setPos(origin);
            PreviewWorld.forget(entity);
        }
        entity.xo = entity.xOld = entity.getX();
        entity.yo = entity.yOld = entity.getY();
        entity.zo = entity.zOld = entity.getZ();

        // Forward is where the mob faces (yaw 0: +z), as the animators tell from the motion.
        double speed = walking ? WALK_SPEED : 0D;
        entity.setPos(entity.getX(), entity.getY(), entity.getZ() + speed);
        entity.setOnGround(true);
        // As LivingEntity#calculateEntityAnimation swings the limbs.
        entity.walkAnimation.update(Math.min((float) speed * 4F, 1F), 0.4F);

        // As LivingEntity#updateSwingTime carries a swing (begun by #swing) through, without haste or fatigue.
        entity.oAttackAnim = entity.attackAnim;
        if (entity.swinging && ++entity.swingTime >= LivingEntity.SWING_DURATION) {
            entity.swinging = false;
        }
        if (!entity.swinging) {
            entity.swingTime = 0;
        }
        entity.attackAnim = (float) entity.swingTime / LivingEntity.SWING_DURATION;

        if (entity instanceof Player player) {
            // The cape follows these; left behind, it would fly up.
            player.xCloakO = player.xCloak = player.getX();
            player.yCloakO = player.yCloak = player.getY();
            player.zCloakO = player.zCloak = player.getZ();
        }

        // As the entity database does for the entities of the level, at the start of a tick.
        LivingEntityData<LivingEntity> data = PreviewWorld.dataOf(entity);
        if (data != null) {
            data.updateClient();
        }
    }

    /** Swings the mob's main arm, as a player's attack does; carried through by {@link #tick}. Nothing before it's made. */
    public void swing() {
        if (entity != null) {
            entity.swing(InteractionHand.MAIN_HAND);
        }
    }

    /**
     * Draws the mob centered at ({@code x}, {@code y}), fit in a square {@code size} wide, in front
     * of what's drawn at depth 0. To be drawn clipped to its frame (a scissor).
     */
    public void draw(GuiGraphics graphics, float x, float y, int size, float partialTicks) {
        if (!made) {
            made = true;
            entity = world != null ? make(world) : null;
            if (entity == null) {
                fallbackIcon = spawnEggOf(defined);
            }
        }

        if (entity != null) {
            try {
                drawEntity(graphics, entity, x, y, size, partialTicks);
            } catch (Throwable e) {
                MoBends.LOGGER.warn("Unable to draw the preview of {}: {}", defined.key(), e.toString());
                if (world != null) {
                    world.remove(entity);
                }
                entity = null;
                fallbackIcon = spawnEggOf(defined);
                Minecraft.getInstance().getEntityRenderDispatcher().setRenderShadow(true);
                Lighting.setupFor3DItems();
            }
        } else {
            drawFallback(graphics, x, y, size);
        }
    }

    private static void drawEntity(GuiGraphics graphics, LivingEntity entity, float x, float y, int size, float partialTicks) {
        // The hitbox's outline from the view, an upright box turned by YAW and seen from TILT above.
        float yaw = (float) Math.toRadians(YAW);
        float tilt = (float) Math.toRadians(TILT);
        float spread = Math.abs((float) Math.cos(yaw)) + Math.abs((float) Math.sin(yaw));
        float width = entity.getBbWidth() * spread;
        float height = entity.getBbHeight() * (float) Math.cos(tilt) + width * (float) Math.sin(tilt);
        float scale = size / Math.max(Math.max(width, height), 0.1F);

        // As data is updated at the start of every frame for the entities of the level.
        LivingEntityData<LivingEntity> data = PreviewWorld.dataOf(entity);
        if (data != null) {
            data.update(partialTicks);
        }

        // A stack of its own, so a render that stops halfway leaves the screen's as it was.
        PoseStack poses = new PoseStack();
        poses.last().pose().set(graphics.pose().last().pose());
        poses.last().normal().set(graphics.pose().last().normal());
        // In front of the frame, and up the right way (as InventoryScreen#renderEntityInInventory),
        // then turned as 1.12 turned it: the inventory's base is 1.12's turned half a turn about y.
        poses.translate(x, y, 100F);
        poses.scale(scale, scale, -scale);
        poses.mulPose(new Quaternionf().rotateZ((float) Math.PI).rotateY((float) Math.PI).rotateX(tilt).rotateY(yaw));
        // Turned about the hitbox's center, which lands on the origin.
        poses.translate(0F, -entity.getBbHeight() / 2F, 0F);

        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        try {
            RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0D, 0D, 0D, 0F, partialTicks, poses, graphics.bufferSource(), LIGHT));
            graphics.flush();
        } finally {
            dispatcher.setRenderShadow(true);
            Lighting.setupFor3DItems();
        }
    }

    private void drawFallback(GuiGraphics graphics, float x, float y, int size) {
        Minecraft mc = Minecraft.getInstance();
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0F);
        if (fallbackIcon != null) {
            float scale = size / 16F;
            graphics.pose().scale(scale, scale, 1F);
            graphics.renderFakeItem(fallbackIcon, -8, -8);
        } else {
            graphics.pose().scale(2F, 2F, 1F);
            graphics.drawString(mc.font, "?", -mc.font.width("?") / 2, -4, 0x808080);
        }
        graphics.pose().popPose();
    }

    /** The entity shown, registered with the preview world; null if one can't be made. */
    @Nullable
    private LivingEntity make(PreviewWorld world) {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null) {
            return null;
        }
        try {
            LivingEntity made = construct(level, player);
            if (made == null) {
                MoBends.LOGGER.warn("Unable to preview {}: no entity of {} can be made.", defined.key(), defined.entityTypeId());
                return null;
            }
            // Standing where the player stands, so what the animators find around it is ground.
            origin = player.position();
            made.moveTo(origin.x, origin.y, origin.z, 0F, 0F);
            // Nothing ticks it to catch the previous rotations up: the head would turn between the two.
            made.yHeadRot = made.yHeadRotO = 0F;
            made.yBodyRot = made.yBodyRotO = 0F;
            made.xo = made.xOld = made.getX();
            made.yo = made.yOld = made.getY();
            made.zo = made.zOld = made.getZ();
            made.setOnGround(true);
            world.register(made, defined);
            setUp.accept(made);
            return made;
        } catch (Throwable e) {
            MoBends.LOGGER.warn("Unable to preview {}: {}", defined.key(), e.toString());
            return null;
        }
    }

    @Nullable
    private LivingEntity construct(ClientLevel level, LocalPlayer player) {
        // A player is never made from its entity type: it's a copy of the player.
        Class<?> entityClass = defined.entityClass();
        if (entityClass != null && entityClass.isAssignableFrom(RemotePlayer.class)
                || ResourceLocation.withDefaultNamespace("player").equals(defined.entityTypeId())) {
            return new PreviewPlayer(level, player);
        }
        EntityType<?> type = entityTypeOf(defined);
        if (type == null) {
            return null;
        }
        Entity made = type.create(level);
        return made instanceof LivingEntity living && defined.animates(living) ? living : null;
    }

    @Nullable
    private static EntityType<?> entityTypeOf(DefinedEntity defined) {
        ResourceLocation id = defined.entityTypeId();
        return id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
    }

    @Nullable
    private static ItemStack spawnEggOf(DefinedEntity defined) {
        SpawnEggItem egg = SpawnEggItem.byId(entityTypeOf(defined));
        return egg == null ? null : new ItemStack(egg);
    }

    /** A copy of the player: their skin, the parts of it they show, their hand and what they hold and wear. */
    private static class PreviewPlayer extends RemotePlayer {
        PreviewPlayer(ClientLevel level, Player source) {
            super(level, source.getGameProfile());
            this.getEntityData().set(PlayerAccessor.mobends$getModeCustomisation(),
                    source.getEntityData().get(PlayerAccessor.mobends$getModeCustomisation()));
            this.setMainArm(source.getMainArm());
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                this.setItemSlot(slot, source.getItemBySlot(slot).copy());
            }
        }
    }
}
