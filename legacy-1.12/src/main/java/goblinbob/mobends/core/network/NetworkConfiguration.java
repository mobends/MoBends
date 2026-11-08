package goblinbob.mobends.core.network;

import goblinbob.mobends.core.kumo.AnimationLimits;

/**
 * The options a server shares with its players (see {@code AnimationPolicy} for what the client
 * does with them). The server's copy is loaded from its config; a client's copy holds the defaults
 * until the server's answer arrives.
 */
public class NetworkConfiguration
{

    public static final NetworkConfiguration instance = new NetworkConfiguration();

    private final SharedConfig sharedConfig = new SharedConfig();
    private final SharedProperty<String> resourcePackAnimation;
    private final SharedProperty<Float> maxPartOffset;
    private final SharedProperty<Float> maxBodyOffset;
    private final SharedProperty<Float> maxPartScale;

    public NetworkConfiguration()
    {
        sharedConfig.addProperty(resourcePackAnimation = new SharedStringProp(
                "resourcePackAnimation",
                ResourcePackPolicy.LIMITED.name(),
                "What players' resource packs may do to Mo' Bends animation (types, extensions, animators, clips): "
                        + "ALLOW (anything), LIMITED (their offsets and scales stay close to the trusted animation's, and their custom model geometry is refused) "
                        + "or DENY (they are ignored). Mods and the server's own resource pack are always trusted."));
        sharedConfig.addProperty(maxPartOffset = new SharedFloatProp(
                "maxPartOffset",
                4F,
                "LIMITED only: how far (in model units, 1/16 block) resource packs' animation may move any single part of a model from where the trusted animation puts it."));
        sharedConfig.addProperty(maxBodyOffset = new SharedFloatProp(
                "maxBodyOffset",
                16F,
                "LIMITED only: how far (in model units, 1/16 block) resource packs' animation may move a whole model from where the trusted animation puts it."));
        sharedConfig.addProperty(maxPartScale = new SharedFloatProp(
                "maxPartScale",
                AnimationLimits.DEFAULT_MAX_PART_SCALE,
                "LIMITED only: by what factor, at most, resource packs' animation may scale any single part of a model, up or down, from the scale the trusted animation gives it (1: not at all). Packs can't hide a part this way, nor turn it inside out."));
    }

    /** Back to the defaults: what a server without Mo' Bends (that never answers) gets. */
    public void resetToDefaults()
    {
        for (SharedProperty<?> property : sharedConfig.getProperties())
        {
            property.reset();
        }
    }

    public SharedConfig getSharedConfig()
    {
        return sharedConfig;
    }

    public ResourcePackPolicy getResourcePackAnimation()
    {
        return ResourcePackPolicy.parse(resourcePackAnimation.getValue());
    }

    public float getMaxPartOffset()
    {
        return Math.max(0, maxPartOffset.getValue());
    }

    public float getMaxBodyOffset()
    {
        return Math.max(0, maxBodyOffset.getValue());
    }

    /** At least 1. */
    public float getMaxPartScale()
    {
        float value = maxPartScale.getValue();
        return value >= 1F ? value : 1F;
    }

}
