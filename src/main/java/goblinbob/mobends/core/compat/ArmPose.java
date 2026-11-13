package goblinbob.mobends.core.compat;

/**
 * The arm poses of 1.9 and later's {@code ModelBiped.ArmPose}, which 1.8.9 keeps as a number ({@code heldItemRight})
 * and a flag ({@code aimedBow}).
 */
public enum ArmPose
{
    EMPTY,
    ITEM,
    BLOCK,
    BOW_AND_ARROW
}
