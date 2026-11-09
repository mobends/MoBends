package goblinbob.mobends.core.client;

import com.google.gson.JsonParseException;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.kumo.AnimationLimits;
import goblinbob.mobends.core.network.ResourcePackPolicy;
import goblinbob.mobends.core.network.ServerRules;
import goblinbob.mobends.core.network.ServerRulesPayload;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;

/**
 * What resource packs may do to animation on the client right now (see {@link ResourcePackPolicy}),
 * and keeping the loaded animation content in line with it: the types, extensions, animators,
 * clips and model definitions are reloaded whenever the policy in force differs from the one they
 * were loaded under. Client thread only.
 */
public final class AnimationPolicy {
    public static final AnimationPolicy INSTANCE = new AnimationPolicy();

    /** The policy the loaded content follows. Until a world is joined everything is allowed. */
    private ResourcePackPolicy loadedWith = ResourcePackPolicy.ALLOW;
    @Nullable
    private AnimationLimits limits;

    private AnimationPolicy() {
    }

    /** The policy in force: the server's, except in singleplayer (and a LAN world's host, and outside a world), where resource packs are allowed. */
    public ResourcePackPolicy current() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.hasSingleplayerServer()) {
            return ResourcePackPolicy.ALLOW;
        }
        return ServerRules.received().resourcePackAnimation();
    }

    /** The limits resource packs' animation is kept to, or null when it isn't limited ({@code AnimationLimits.current}). */
    @Nullable
    public AnimationLimits limits() {
        if (current() != ResourcePackPolicy.LIMITED) {
            return null;
        }
        ServerRules rules = ServerRules.received();
        float part = rules.maxPartOffset();
        float body = rules.maxBodyOffset();
        float scale = rules.maxPartScale();
        if (limits == null || limits.maxPartOffset != part || limits.maxBodyOffset != body || limits.maxPartScale != scale) {
            limits = new AnimationLimits(part, body, scale);
        }
        return limits;
    }

    /** The animation content was just dropped, to be loaded again under the policy in force. */
    public void onContentReloaded() {
        loadedWith = current();
    }

    /** A world was joined: the defaults apply until the server's rules arrive (a server without Mo' Bends sends none). */
    public void onWorldJoin() {
        ServerRules.resetReceived();
        reloadIfChanged();
    }

    /** The server's rules arrived. */
    public void onServerRules(ServerRulesPayload payload) {
        try {
            ServerRules.setReceived(ServerRules.fromJson(payload.json()));
        } catch (JsonParseException e) {
            MoBends.LOGGER.error("Mo' Bends: the server's rules can't be read; the defaults apply.", e);
            ServerRules.resetReceived();
        }
        MoBends.LOGGER.info("Mo' Bends: the server's rules: {}", ServerRules.received());
        reloadIfChanged();
    }

    private void reloadIfChanged() {
        if (current() != loadedWith) {
            CoreClient.reloadAnimation();
        }
    }
}
