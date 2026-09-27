package goblinbob.mobends.core.kumo;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * How far animation from untrusted sources (see {@code IKumoInstancingContext#isTrusted}) may
 * take a model, when the host limits it: every part's offset and the whole body's offsets may
 * differ from what the trusted animation gives them by at most these many model units.
 * Rotations are free; animation can't scale parts.
 */
public final class AnimationLimits
{

    /** The limits in force, or null for none; the mod installs one backed by the server's settings. */
    private static volatile Supplier<AnimationLimits> provider = () -> null;

    public final float maxPartOffset;
    public final float maxBodyOffset;

    public AnimationLimits(float maxPartOffset, float maxBodyOffset)
    {
        this.maxPartOffset = maxPartOffset;
        this.maxBodyOffset = maxBodyOffset;
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
