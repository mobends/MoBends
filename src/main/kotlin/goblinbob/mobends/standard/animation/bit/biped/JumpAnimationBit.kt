package goblinbob.mobends.standard.animation.bit.biped

import goblinbob.mobends.core.animation.bit.AnimationBit
import goblinbob.mobends.standard.data.BipedEntityData
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.MathHelper
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max

/**
 * Represents a jump animation bit for biped entities.
 */
class JumpAnimationBit<T : BipedEntityData<*>> : AnimationBit<T>() {
  /**
   * All constants that are shared across all bits of this type.
   */
  companion object {
    /*
     * Holds the actions that the bits of this type perform.
     * TODO: Used by bends-packs, but not sure for purpose exactly.
     */
    private val ACTIONS = arrayOf("jump")
  }

  override fun getActions(entityData: T): Array<String> = ACTIONS

  override fun onPlay(data: T) {
    data.renderRotation.identity()
    data.centerRotation.identity()
    data.body.rotation.orientInstantX(20f)
    data.rightLeg.rotation.orientInstantX(0f)
    data.leftLeg.rotation.orientInstantX(0f)
    data.rightForeLeg.rotation.orientInstantX(0f)
    data.leftForeLeg.rotation.orientInstantX(0f)
    data.rightArm.rotation.orientInstantZ(2f)
    data.leftArm.rotation.orientInstantZ(-2f)
    data.rightForeArm.rotation.orientInstantX(-20f)
    data.leftForeArm.rotation.orientInstantX(-20f)
  }

  override fun perform(data: T) {
    if (data.prevMotionY < 0 && data.motionY > 0) {
      /*
       * Restarting the animation if the player is going back up again after falling
       * down.
       */
      onPlay(data)
    }

    data.globalOffset.slideToZero(0.3f)
    data.renderRotation.setSmoothness(0.3f).orientZero()
    data.centerRotation.setSmoothness(0.7f).orientZero()
    data.renderRightItemRotation.setSmoothness(0.3f).orientZero()
    data.renderLeftItemRotation.setSmoothness(0.3f).orientZero()

    val bodyRotationX = max(1.0f - data.ticksInAir * 0.1f, 0.0f)
    data.body.rotation.setSmoothness(0.2f).orientX(bodyRotationX)
    data.rightArm.rotation.setSmoothness(0.05f).orientZ(45f)
    data.leftArm.rotation.setSmoothness(0.05f).orientZ(-45f)
    data.rightForeArm.rotation.setSmoothness(0.3f).orientX(0f)
    data.leftForeArm.rotation.setSmoothness(0.3f).orientX(0f)

    data.head.rotation.orientX(data.headPitch.get() - bodyRotationX).rotateY(data.headYaw.get())

    if (!data.isStillHorizontally()) {
      val limbSwing = data.limbSwing.get() * 0.6662f
      val limbSwingAmount = 0.7f * data.limbSwingAmount.get() / PI.toFloat() * 180f

      data.rightLeg.rotation.setSmoothness(1.0f).orientX(-5f + MathHelper.cos(limbSwing) * limbSwingAmount)
      data.leftLeg.rotation.setSmoothness(1.0f)
        .orientX(-5f + MathHelper.cos(limbSwing + PI.toFloat()) * limbSwingAmount)

      val limbSwingVar = (limbSwing / PI.toFloat()) % 2
      data.leftForeLeg.rotation.setSmoothness(0.3f).orientX(if (limbSwingVar > 1) 45f else 0f)
      data.rightForeLeg.rotation.setSmoothness(0.3f).orientX(if (limbSwingVar > 1) 0f else 45f)
      data.leftForeArm.rotation.setSmoothness(0.3f)
        .orientX((MathHelper.cos(limbSwing + PI.toFloat() / 2) / 2f + 0.5f) * -20f)
      data.rightForeArm.rotation.setSmoothness(0.3f).orientX((MathHelper.cos(limbSwing) / 2f + 0.5f) * -20f)
    } else {
      data.rightLeg.rotation.setSmoothness(0.1f).orientZ(10f)
      data.rightLeg.rotation.setSmoothness(0.3f).rotateX(-45f)
      data.leftLeg.rotation.setSmoothness(0.1f).orientZ(-10f)
      data.leftLeg.rotation.setSmoothness(0.3f).rotateX(-17f)
      data.rightForeLeg.rotation.setSmoothness(0.3f).orientX(70f)
      data.leftForeLeg.rotation.setSmoothness(0.3f).orientX(17f)
    }
  }
}
