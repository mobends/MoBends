package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.*;
import goblinbob.mobends.standard.animation.bit.biped.item.*;
import goblinbob.mobends.standard.animation.bit.player.*;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
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

    protected final BipedActionController actionController = new BipedActionController();

    protected ArmatureMask upperBodyOnlyMask;

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

    public void performActionAnimations(PlayerData data, AbstractClientPlayer player)
    {
        if (player.isEntityAlive() && player.isPlayerSleeping())
        {
            actionController.clearAction();
            return;
        }

        final EnumHandSide primaryHand = player.getPrimaryHand();
        final ItemStack heldItemMainhand = player.getHeldItemMainhand();
        final ItemStack heldItemOffhand = player.getHeldItemOffhand();
        final Item activeItem = player.getActiveItemStack().getItem();

        actionController.perform(data, primaryHand, heldItemMainhand, heldItemOffhand, activeItem);
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


        // Resetting item rotations
        data.renderLeftItemRotation.orientZero();
        data.renderRightItemRotation.orientZero();

        final List<String> actions = new ArrayList<>();
        layerBase.perform(data, actions);
        layerSneak.perform(data, actions);
        layerTorch.perform(data, actions);
        this.performActionAnimations(data, player);
        layerCape.perform(data, actions);
        return actions;
    }
}
