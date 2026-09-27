package goblinbob.mobends.core.kumo;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * How far animation from untrusted sources (resource packs, see {@code PackTrust}) may take a
 * model, when the server limits it: every part's offset and the whole body's offsets may differ
 * from what the trusted animation gives them by at most these many model units (1/16 block).
 * Rotations are free. Scale needs no limit: animation can't scale parts, and untrusted model
 * geometry is refused under the same policy.
 */
public final class AnimationLimits
{

    /** The limits in force, or null for none; the mod installs one backed by the server's settings. */
    private static Supplier<AnimationLimits> provider = () -> null;

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
