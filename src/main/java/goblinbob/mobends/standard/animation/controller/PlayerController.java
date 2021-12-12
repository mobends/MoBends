package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.UseActionType;
import goblinbob.mobends.standard.animation.bit.biped.*;
import goblinbob.mobends.standard.animation.bit.biped.item.*;
import goblinbob.mobends.standard.animation.bit.player.*;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PlayerData;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is an animation controller for a player instance. It's a part of the EntityData structure.
 *
 * @author Iwo Plaza
 */
public class PlayerController implements IAnimationController<PlayerData>
{
    protected HardAnimationLayer<BipedEntityData<?>> layerBase = new HardAnimationLayer<>();
    protected HardAnimationLayer<BipedEntityData<?>> layerTorch = new HardAnimationLayer<>();
    protected HardAnimationLayer<BipedEntityData<?>> layerSneak = new HardAnimationLayer<>();
    protected HardAnimationLayer<BipedEntityData<?>> layerAction = new HardAnimationLayer<>();
    protected HardAnimationLayer<BipedEntityData<?>> layerCape = new HardAnimationLayer<>();

    protected AnimationBit<BipedEntityData<?>> bitStand = new StandAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitJump = new JumpAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitSneak = new SneakAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitLadderClimb = new LadderClimbAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitSwimming = new SwimmingAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitRiding = new RidingAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitSitting = new SittingAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitFalling = new FallingAnimationBit();
    protected AnimationBit<PlayerData> bitWalk = new goblinbob.mobends.standard.animation.bit.player.WalkAnimationBit();
    protected AnimationBit<PlayerData> bitSprint = new goblinbob.mobends.standard.animation.bit.player.SprintAnimationBit();
    protected AnimationBit<PlayerData> bitSprintJump = new SprintJumpAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitTorchHolding = new TorchHoldingAnimationBit();
    protected FlyingAnimationBit bitFlying = new FlyingAnimationBit();
    protected ElytraAnimationBit bitElytra = new ElytraAnimationBit();
    protected CapeAnimationBit bitCape = new CapeAnimationBit();
    protected SleepingAnimationBit bitSleeping = new SleepingAnimationBit();

    protected UseActionType currentUseActionType = null;
    protected AttackActionType currentAttackActionType = null;
    protected AnimationBit<BipedEntityData<?>> actionBit = null;

    protected ArmatureMask upperBodyOnlyMask;

    private static final Map<UseActionType, ItemActionFactory<AnimationBit<BipedEntityData<?>>>> ITEM_USE_ACTION_MAP = new HashMap<>();
    private static final Map<AttackActionType, ItemActionFactory<AnimationBit<BipedEntityData<?>>>> ITEM_ATTACK_ACTION_MAP = new HashMap<>();
    static
    {
        ITEM_USE_ACTION_MAP.put(UseActionType.FOOD, EatingAnimationBit::new);
        ITEM_USE_ACTION_MAP.put(UseActionType.BOW, BowAction::new);
        ITEM_USE_ACTION_MAP.put(UseActionType.SHIELD, ShieldAnimationBit::new);

        ITEM_ATTACK_ACTION_MAP.put(AttackActionType.TOOL, ToolAction::new);
        ITEM_ATTACK_ACTION_MAP.put(AttackActionType.FISTS, PunchingAction::new);
        ITEM_ATTACK_ACTION_MAP.put(AttackActionType.SWORD, SwordAction::new);
        ITEM_ATTACK_ACTION_MAP.put(AttackActionType.LONGSWORD, SwordAction::new);

        // Completeness checks
        for (UseActionType type : UseActionType.values())
        {
            if (!ITEM_USE_ACTION_MAP.containsKey(type))
                throw new IllegalStateException("The ITEM_USE_ACTION_MAP map needs to be complete.");
        }

        // Completeness checks
        for (AttackActionType type : AttackActionType.values())
        {
            if (!ITEM_ATTACK_ACTION_MAP.containsKey(type))
                throw new IllegalStateException("The ITEM_ATTACK_ACTION_MAP map needs to be complete.");
        }
    }

    public PlayerController()
    {
        this.upperBodyOnlyMask = new ArmatureMask(ArmatureMask.Mode.EXCLUDE_ONLY);
        this.upperBodyOnlyMask.exclude("root");
        this.upperBodyOnlyMask.exclude("head");
        this.upperBodyOnlyMask.exclude("leftLeg");
        this.upperBodyOnlyMask.exclude("leftForeLeg");
        this.upperBodyOnlyMask.exclude("rightLeg");
        this.upperBodyOnlyMask.exclude("rightForeLeg");
    }

    public static UseActionType getBuiltInItemUseAction(Item item, ModelBiped.ArmPose armPoseMain, ModelBiped.ArmPose armPoseOff)
    {
        if (item == Items.AIR)
            return null;

        if (item instanceof ItemFood)
            return UseActionType.FOOD;

        if (item instanceof ItemBow || armPoseMain == ArmPose.BOW_AND_ARROW || armPoseOff == ArmPose.BOW_AND_ARROW)
            return UseActionType.BOW;

        if (armPoseMain == ArmPose.BLOCK || armPoseOff == ArmPose.BLOCK)
            return UseActionType.SHIELD;

        return UseActionType.FOOD;
    }

    public static UseActionType getItemUseAction(Item item, ModelBiped.ArmPose armPoseMain, ModelBiped.ArmPose armPoseOff)
    {
        UseActionType useActionType = ModConfig.getItemUseAction(item);

        return useActionType != null ? useActionType : getBuiltInItemUseAction(item, armPoseMain, armPoseOff);
    }

    public static AttackActionType getBuiltInItemAttackAction(Item item)
    {
        if (item instanceof ItemSword)
            return AttackActionType.SWORD;

        if (item == Items.AIR)
            return AttackActionType.FISTS;

        return AttackActionType.TOOL;
    }

    public static AttackActionType getItemAttackAction(Item item)
    {
        AttackActionType attackActionType = ModConfig.getItemAttackAction(item);

        return attackActionType != null ? attackActionType : getBuiltInItemAttackAction(item);
    }

    public void performActionAnimations(PlayerData data, AbstractClientPlayer player)
    {
        if (player.isEntityAlive() && player.isPlayerSleeping())
        {
            layerAction.clearAnimation();
            return;
        }

        final EnumHandSide primaryHand = player.getPrimaryHand();
        final EnumHandSide offHand = primaryHand == EnumHandSide.RIGHT ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
        final ItemStack heldItemMainhand = player.getHeldItemMainhand();
        final ItemStack heldItemOffhand = player.getHeldItemOffhand();
        final Item activeItem = player.getActiveItemStack().getItem();
        final ModelBiped.ArmPose armPoseMain = getAction(player, heldItemMainhand);
        final ModelBiped.ArmPose armPoseOff = getAction(player, heldItemOffhand);
        final EnumHandSide activeHandSide = player.getActiveHand() == EnumHand.MAIN_HAND ? primaryHand : offHand;

        UseActionType useActionType = getItemUseAction(activeItem, armPoseMain, armPoseOff);
        if (useActionType != currentUseActionType)
        {
            currentUseActionType = useActionType;

            if (useActionType != null)
            {
                ItemActionFactory<AnimationBit<BipedEntityData<?>>> factory = ITEM_USE_ACTION_MAP.get(useActionType);
                this.actionBit = factory.create(activeHandSide);
                this.layerAction.playOrContinueBit(this.actionBit, data);
            }
            else
            {
                this.layerAction.clearAnimation();
                this.currentAttackActionType = null;
            }
        }

        AttackActionType attackActionType = getItemAttackAction(heldItemMainhand.getItem());

        if (this.currentAttackActionType != attackActionType)
        {
            this.currentAttackActionType = attackActionType;

            ItemActionFactory<AnimationBit<BipedEntityData<?>>> factory = ITEM_ATTACK_ACTION_MAP.get(attackActionType);
            if (factory == null)
            {
                this.actionBit = null;
                this.layerAction.clearAnimation();
            }
            else
            {
                this.actionBit = factory.create(primaryHand);
                this.layerAction.playOrContinueBit(this.actionBit, data);
            }
        }
    }

    @Override
    public Collection<String> perform(PlayerData data)
    {
        final AbstractClientPlayer player = data.getEntity();

        layerCape.playOrContinueBit(bitCape, data);

        if (player.isEntityAlive() && player.isPlayerSleeping())
        {
            layerBase.playOrContinueBit(bitSleeping, data);
            layerSneak.clearAnimation();
        }
        else if (player.isRiding())
        {
            if (player.getRidingEntity() instanceof EntityLivingBase)
            {
                layerBase.playOrContinueBit(bitRiding, data);
            }
            else
            {
                layerBase.playOrContinueBit(bitSitting, data);
            }
            layerSneak.clearAnimation();
        }
        else
        {
            if (player.getTicksElytraFlying() > 4)
            {
                layerBase.playOrContinueBit(bitElytra, data);
                layerSneak.clearAnimation();
                layerTorch.clearAnimation();
            }
            else if (data.isClimbing())
            {
                layerBase.playOrContinueBit(bitLadderClimb, data);
                layerSneak.clearAnimation();
                layerTorch.clearAnimation();
            }
            else if (player.isInWater())
            {
                layerBase.playOrContinueBit(bitSwimming, data);
                layerSneak.clearAnimation();
                layerTorch.clearAnimation();
            }
            else if (!data.isOnGround() || data.getTicksAfterTouchdown() < 1)
            {
                // Airborne
                if (data.isFlying())
                {
                    // Flying
                    layerBase.playOrContinueBit(bitFlying, data);
                }
                else
                {
                    if (data.getTicksFalling() > FallingAnimationBit.TICKS_BEFORE_FALLING)
                    {
                        layerBase.playOrContinueBit(bitFalling, data);
                    }
                    else
                    {
                        if (player.isSprinting())
                            layerBase.playOrContinueBit(bitSprintJump, data);
                        else
                            layerBase.playOrContinueBit(bitJump, data);
                    }
                }

                layerSneak.clearAnimation();
                layerTorch.clearAnimation();
            }
            else
            {
                if (data.isStillHorizontally())
                {
                    layerBase.playOrContinueBit(bitStand, data);
                    layerTorch.playOrContinueBit(bitTorchHolding, data);
                }
                else
                {
                    if (player.isSprinting())
                    {
                        layerBase.playOrContinueBit(bitSprint, data);
                        layerTorch.clearAnimation();
                    }
                    else
                    {
                        layerBase.playOrContinueBit(bitWalk, data);
                        layerTorch.playOrContinueBit(bitTorchHolding, data);
                    }
                }

                if (player.isSneaking())
                    layerSneak.playOrContinueBit(bitSneak, data);
                else
                    layerSneak.clearAnimation();
            }
        }

        this.performActionAnimations(data, player);

        final List<String> actions = new ArrayList<>();
        layerBase.perform(data, actions);
        layerSneak.perform(data, actions);
        layerTorch.perform(data, actions);
        layerAction.perform(data, actions);
        layerCape.perform(data, actions);
        return actions;
    }

    private static ArmPose getAction(AbstractClientPlayer player, ItemStack heldItem)
    {
        if (!heldItem.isEmpty())
        {
            if (player.getItemInUseCount() > 0)
            {
                EnumAction enumaction = heldItem.getItemUseAction();

                if (enumaction == EnumAction.BLOCK)
                    return ArmPose.BLOCK;
                else if (enumaction == EnumAction.BOW)
                    return ArmPose.BOW_AND_ARROW;
            }

            return ArmPose.ITEM;
        }

        return ArmPose.EMPTY;
    }
}
