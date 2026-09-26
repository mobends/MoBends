package goblinbob.mobends.core.network;

import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.kumo.AnimationLimits;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * These are options that are provided by a server the player's playing on.
 * They are default when playing on single-player.
 */
public class NetworkConfiguration
{

    public static NetworkConfiguration instance = new NetworkConfiguration();

    private final SharedConfig sharedConfig = new SharedConfig();
    private final SharedProperty<String> resourcePackAnimation;
    private final SharedProperty<Float> maxPartOffset;
    private final SharedProperty<Float> maxBodyOffset;

    /** What the animation was last loaded with (see {@link #applyChanges()}). */
    @Nullable
    private String applied;

    public NetworkConfiguration()
    {
        sharedConfig.addProperty(resourcePackAnimation = new SharedStringProp(
                "resourcePackAnimation",
                ResourcePackPolicy.LIMITED.name(),
                "What players' resource packs may do to Mo' Bends animation (types, extensions, animators, clips): "
                        + "ALLOW (anything), LIMITED (their offsets stay close to the trusted animation's, and their custom model geometry is refused) "
                        + "or DENY (they are ignored). Mods and the server's own resource pack are always trusted."));
        sharedConfig.addProperty(maxPartOffset = new SharedFloatProp(
                "maxPartOffset",
                4F,
                "LIMITED only: how far (in model units, 1/16 block) resource packs' animation may move any single part of a model from where the trusted animation puts it."));
        sharedConfig.addProperty(maxBodyOffset = new SharedFloatProp(
                "maxBodyOffset",
                16F,
                "LIMITED only: how far (in model units, 1/16 block) resource packs' animation may move a whole model from where the trusted animation puts it."));
    }

    /**
     * Sets up the default permissions before receiving the server's config: a server that doesn't
     * answer (no Mo' Bends on it) gets the defaults, which limit resource packs.
     */
    public void onWorldJoin()
    {
        this.resourcePackAnimation.setValue(this.resourcePackAnimation.getDefaultValue());
        this.maxPartOffset.setValue(this.maxPartOffset.getDefaultValue());
        this.maxBodyOffset.setValue(this.maxBodyOffset.getDefaultValue());
        applyChanges();
    }

    public SharedConfig getSharedConfig()
    {
        return sharedConfig;
    }

    /** The policy in force: the server's, except in singleplayer, where resource packs are always allowed. */
    public ResourcePackPolicy getResourcePackPolicy()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.isSingleplayer() || mc.world == null)
        {
            return ResourcePackPolicy.ALLOW;
        }
        return ResourcePackPolicy.parse(resourcePackAnimation.getValue());
    }

    /** The limits resource packs' animation is kept to, or null when it isn't limited. */
    @Nullable
    public AnimationLimits getAnimationLimits()
    {
        if (getResourcePackPolicy() != ResourcePackPolicy.LIMITED)
        {
            return null;
        }
        return new AnimationLimits(Math.max(0, maxPartOffset.getValue()), Math.max(0, maxBodyOffset.getValue()));
    }

    /**
     * Reloads the entity types and animators if the policy or the limits changed since they were
     * loaded (resource packs' types and extensions are ignored or not, their models refused or not).
     * Runs on the client thread.
     */
    public void applyChanges()
    {
        String now = getResourcePackPolicy() + " " + maxPartOffset.getValue() + " " + maxBodyOffset.getValue();
        if (Objects.equals(now, applied))
        {
            return;
        }
        boolean first = applied == null;
        applied = now;
        if (!first)
        {
            Minecraft.getMinecraft().addScheduledTask(CoreClient::reloadAnimation);
        }
    }

}
