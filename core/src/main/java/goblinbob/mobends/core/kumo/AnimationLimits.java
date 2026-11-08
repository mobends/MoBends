package goblinbob.mobends.core.kumo;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * How far animation from untrusted sources (see {@code IKumoInstancingContext#isTrusted}) may
 * take a model, when the host limits it: every part's offset and the whole body's offsets may
 * differ from what the trusted animation gives them by at most these many model units, and every
 * part's scale, on each axis, by at most a factor of {@link #maxPartScale} either way (so a part
 * can't be hidden by scaling it to nothing, nor blown up to cover the screen). Rotations are free.
 */
public final class AnimationLimits
{

    /** The limits in force, or null for none; the mod installs one backed by the server's settings. */
    private static volatile Supplier<AnimationLimits> provider = () -> null;

    /** What {@link #maxPartScale} is when the host doesn't say. */
    public static final float DEFAULT_MAX_PART_SCALE = 1.5F;

    public final float maxPartOffset;
    public final float maxBodyOffset;
    /** At least 1 (1: untrusted animation can't scale parts). */
    public final float maxPartScale;

    public AnimationLimits(float maxPartOffset, float maxBodyOffset)
    {
        this(maxPartOffset, maxBodyOffset, DEFAULT_MAX_PART_SCALE);
    }

    /** @param maxPartScale less than 1 (or NaN) counts as 1. */
    public AnimationLimits(float maxPartOffset, float maxBodyOffset, float maxPartScale)
    {
        this.maxPartOffset = maxPartOffset;
        this.maxBodyOffset = maxBodyOffset;
        this.maxPartScale = maxPartScale >= 1F ? maxPartScale : 1F;
    }

    @Nullable
    public static AnimationLimits current()
    {
        return provider.get();
    }

    public static void setProvider(Supplier<AnimationLimits> limits)
    {
        provider = limits;
    }

}
