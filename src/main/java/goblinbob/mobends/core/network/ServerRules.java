package goblinbob.mobends.core.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.kumo.AnimationLimits;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * What a server allows its players' resource packs to do to Mo' Bends animation (1.12's
 * {@code NetworkConfiguration}): kept in the server's {@code config/mobends-server.json} and sent to
 * every player who joins with Mo' Bends ({@link ServerRulesPayload}). A client holds the defaults
 * until the server's arrive, and keeps them on a server without Mo' Bends (see
 * {@code AnimationPolicy} for what the client does with them).
 */
public final class ServerRules {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String FILE_NAME = "mobends-server.json";

    /** This side's server's rules, read from its config (a dedicated server's, or the integrated one's, opened to LAN). */
    private static ServerRules server = new ServerRules();
    /** The rules of the server the client is on: the defaults until they arrive. Client thread only. */
    private static ServerRules received = new ServerRules();

    /**
     * {@code ALLOW}, {@code LIMITED} or {@code DENY} (see {@link ResourcePackPolicy}): what players'
     * resource packs may do to animation (types, extensions, animators, clips). Mods and the
     * server's own resource pack are always trusted.
     */
    String resourcePackAnimation = ResourcePackPolicy.LIMITED.name();
    /** LIMITED only: how far (model units, 1/16 block) packs may move any single part from where the trusted animation puts it. */
    float maxPartOffset = 4F;
    /** LIMITED only: how far (model units) packs may move a whole model from where the trusted animation puts it. */
    float maxBodyOffset = 16F;
    /** LIMITED only: by what factor, at most, packs may scale a part, up or down, from its trusted scale (1: not at all). */
    float maxPartScale = AnimationLimits.DEFAULT_MAX_PART_SCALE;

    public ResourcePackPolicy resourcePackAnimation() {
        return ResourcePackPolicy.parse(resourcePackAnimation);
    }

    public float maxPartOffset() {
        return Math.max(0, maxPartOffset);
    }

    public float maxBodyOffset() {
        return Math.max(0, maxBodyOffset);
    }

    /** At least 1. */
    public float maxPartScale() {
        return maxPartScale >= 1F ? maxPartScale : 1F;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    /** The rules {@code json} holds; what it lacks keeps its default. */
    public static ServerRules fromJson(String json) throws JsonParseException {
        ServerRules rules = GSON.fromJson(json, ServerRules.class);
        if (rules == null) {
            throw new JsonParseException("empty");
        }
        if (rules.resourcePackAnimation == null) {
            rules.resourcePackAnimation = ResourcePackPolicy.LIMITED.name();
        }
        return rules;
    }

    /** Reads this side's server's rules from {@code config/mobends-server.json}, writing the defaults the first time. */
    public static void loadServerFile(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                server = GSON.fromJson(reader, ServerRules.class);
                if (server == null || server.resourcePackAnimation == null) {
                    throw new JsonParseException("missing resourcePackAnimation");
                }
            } catch (IOException | JsonParseException e) {
                MoBends.LOGGER.error("Could not read {}; the defaults are used.", file, e);
                server = new ServerRules();
            }
            return;
        }
        server = new ServerRules();
        try {
            Files.createDirectories(configDir);
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(server, writer);
            }
        } catch (IOException e) {
            MoBends.LOGGER.error("Could not write {}", file, e);
        }
    }

    /** The rules this side's server sends its players. */
    public static ServerRules server() {
        return server;
    }

    /** The rules of the server the client is on (the defaults until they arrive). */
    public static ServerRules received() {
        return received;
    }

    /** The client got its server's rules. */
    public static void setReceived(ServerRules rules) {
        received = rules;
    }

    /** Back to the defaults: what a server without Mo' Bends (that never sends any) gets. */
    public static void resetReceived() {
        received = new ServerRules();
    }

    @Override
    public String toString() {
        return "resourcePackAnimation=" + resourcePackAnimation() + ", maxPartOffset=" + maxPartOffset() + ", maxBodyOffset=" + maxBodyOffset()
                + ", maxPartScale=" + maxPartScale();
    }
}
