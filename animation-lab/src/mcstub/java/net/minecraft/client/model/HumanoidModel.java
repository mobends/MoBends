package net.minecraft.client.model;

/** Only its name and its arm poses: the lab draws nothing. */
public class HumanoidModel {
    /** What an arm does with the item it holds. */
    public enum ArmPose {
        EMPTY,
        ITEM,
        BLOCK,
        BOW_AND_ARROW,
        THROW_SPEAR,
        CROSSBOW_CHARGE,
        CROSSBOW_HOLD,
        SPYGLASS,
        TOOT_HORN,
        BRUSH
    }
}
