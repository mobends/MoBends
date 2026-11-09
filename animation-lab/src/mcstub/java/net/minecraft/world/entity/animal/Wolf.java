package net.minecraft.world.entity.animal;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Wolf extends LivingEntity {
    public Wolf(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    public Wolf(Level level) {
        this(EntityType.WOLF, level);
    }

    private boolean inSittingPose;
    /** Lab: set by the script (the game's are private, moved by the wolf's tick). */
    public float interestedAngle, interestedAngleO;
    public float shakeAnim, shakeAnimO;

    public boolean isInSittingPose() {
        return inSittingPose;
    }

    public void setInSittingPose(boolean sitting) {
        this.inSittingPose = sitting;
    }

    /** How far the head tilts as the wolf begs, in radians; the game's maths. */
    public float getHeadRollAngle(float partialTicks) {
        return Mth.lerp(partialTicks, interestedAngleO, interestedAngle) * 0.15F * (float) Math.PI;
    }

    /** How far the body rolls as the wolf shakes off water, in radians; the game's maths. */
    public float getBodyRollAngle(float partialTicks, float offset) {
        float f = (Mth.lerp(partialTicks, shakeAnimO, shakeAnim) + offset) / 1.8F;
        if (f < 0.0F) {
            f = 0.0F;
        } else if (f > 1.0F) {
            f = 1.0F;
        }
        return Mth.sin(f * (float) Math.PI) * Mth.sin(f * (float) Math.PI * 11.0F) * 0.15F * (float) Math.PI;
    }

    /** The tail's angle, in radians: the lab's wolves are wild and calm. */
    public float getTailAngle() {
        return (float) (Math.PI / 5);
    }
}
