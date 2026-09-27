package goblinbob.mobends.core.client;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.kumo.AnimationLimits;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.network.ResourcePackPolicy;
import goblinbob.mobends.core.network.SharedProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * What resource packs may do to animation on the client right now (see {@link ResourcePackPolicy}),
 * and keeping the loaded animation content in line with it: the types, extensions, animators,
 * clips and model definitions are reloaded whenever the policy in force differs from the one they
 * were loaded under. Client thread only.
 */
public final class AnimationPolicy
{

    public static final AnimationPolicy INSTANCE = new AnimationPolicy();

    /** The policy the loaded content follows. Until a world is joined everything is allowed. */
    private ResourcePackPolicy loadedWith = ResourcePackPolicy.ALLOW;
    @Nullable
    private AnimationLimits limits;

    private AnimationPolicy()
    {
    }

    /** The policy in force: the server's, except in singleplayer (and outside a world), where resource packs are allowed. */
    public ResourcePackPolicy current()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.world == null || mc.isSingleplayer())
        {
            return ResourcePackPolicy.ALLOW;
        }
        return NetworkConfiguration.instance.getResourcePackAnimation();
    }

    /** The limits resource packs' animation is kept to, or null when it isn't limited. */
    @Nullable
    public AnimationLimits limits()
    {
        if (current() != ResourcePackPolicy.LIMITED)
        {
            return null;
        }
        float part = NetworkConfiguration.instance.getMaxPartOffset();
        float body = NetworkConfiguration.instance.getMaxBodyOffset();
        if (limits == null || limits.maxPartOffset != part || limits.maxBodyOffset != body)
        {
            limits = new AnimationLimits(part, body);
        }
        return limits;
    }

    /** The animation content was just dropped, to be loaded again under the policy in force. */
    public void onContentReloaded()
    {
        loadedWith = current();
    }

    /** A world was joined: the defaults apply until the server's configuration arrives. */
    public void onWorldJoin()
    {
        NetworkConfiguration.instance.resetToDefaults();
        reloadIfChanged();
    }

    /** The server's configuration arrived. */
    public void onServerConfiguration(NBTTagCompound tag)
    {
        NetworkConfiguration.instance.getSharedConfig().readFromNBT(tag);
        StringBuilder builder = new StringBuilder("Received Mo' Bends server configuration.\n");
        for (SharedProperty<?> property : NetworkConfiguration.instance.getSharedConfig().getProperties())
        {
            builder.append(String.format(" - %s: %s\n", property.getKey(), property.getValue()));
        }
        Core.LOG.info(builder.toString());
        reloadIfChanged();
    }

    private void reloadIfChanged()
    {
        if (current() != loadedWith)
        {
            CoreClient.reloadAnimation();
        }
    }

}
