package goblinbob.mobends.core.connection;

import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.util.ApiErrorException;
import goblinbob.mobends.core.util.ConnectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Links the player's Minecraft account to their Mo' Bends account (1.12's {@code MinecraftLink}),
 * proving they own it the way an online-mode server does: the API hands out a server id for the
 * code from the dashboard ({@link #start}), the game joins that "server" on Mojang's session server,
 * and the API checks the join with Mojang ({@link #confirm}). The access token never leaves the game.
 * <p>
 * The two steps are the link screen's: the player enters the code, {@link #start} says which Mo'
 * Bends account it belongs to, the player confirms, and {@link #confirm} links and signs the game
 * in ({@link MoBendsAccount}). Each runs in the background and calls back on the render thread;
 * a screen that may have closed meanwhile checks that it's still open.
 * <p>
 * {@link #confirm} must not run while the game connects to a server: a join in the middle of that
 * can make the connection fail with "Failed to verify username". The Mo' Bends menu is only
 * reachable in a world, where no connection is being made.
 */
public final class MinecraftLink {
    private MinecraftLink() {
    }

    /**
     * What {@link #start} found: the code, the server id to join, and the name of the Mo' Bends
     * account the code belongs to (for the player to confirm).
     */
    public record Start(String code, String serverId, String displayName) {
    }

    /**
     * A finished link.
     *
     * @param uuid        the player's UUID, undashed, as Mojang verified it
     * @param username    the player's username, as Mojang spells it
     * @param displayName the name of the Mo' Bends account the Minecraft account is now linked to
     */
    public record Result(String uuid, String username, String displayName) {
    }

    /** Why linking failed, as the link screen tells the player ({@link #translationKey}). */
    public enum Failure {
        /** The API doesn't know the code (mistyped, expired, or replaced by a newer one). */
        UNKNOWN_CODE,
        /** The join worked, but the code was replaced or expired before the API checked it. */
        UNKNOWN_CODE_CONFIRMING,
        NOT_STARTED,
        INVALID_REQUEST,
        /** Mojang didn't confirm the join to the API. */
        JOIN_NOT_VERIFIED,
        /** The API couldn't reach Mojang's session server. */
        SESSION_SERVER_ERROR,
        /** Any other error from the API. */
        API,
        /** The API couldn't be reached. */
        CONNECTION,
        /** Mojang rejected the game's session (an offline or development launch, an expired session). */
        SESSION,
        /** Mojang's session server couldn't be reached. */
        MOJANG_UNAVAILABLE;

        /** {@code mobends.gui.link.error.<name>}, the message 1.12's link screen showed. */
        public String translationKey() {
            return "mobends.gui.link.error." + name().toLowerCase(Locale.ROOT);
        }

        /** Whether the code can't be used again: the player needs a new one from the dashboard. */
        public boolean codeSpent() {
            return this == UNKNOWN_CODE || this == UNKNOWN_CODE_CONFIRMING;
        }

        static Failure of(Exception e, boolean confirming) {
            if (e instanceof AuthenticationUnavailableException) {
                return MOJANG_UNAVAILABLE;
            }
            if (e instanceof AuthenticationException) {
                return SESSION;
            }
            if (e instanceof ApiErrorException apiError) {
                String error = apiError.getError();
                if (error == null) {
                    return API;
                }
                return switch (error) {
                    case "unknown_code" -> confirming ? UNKNOWN_CODE_CONFIRMING : UNKNOWN_CODE;
                    case "not_started" -> NOT_STARTED;
                    case "invalid_request" -> INVALID_REQUEST;
                    case "join_not_verified" -> JOIN_NOT_VERIFIED;
                    case "session_server_error" -> SESSION_SERVER_ERROR;
                    default -> API;
                };
            }
            return CONNECTION;
        }
    }

    /**
     * Starts linking with the code from the dashboard, as the player typed it (the API ignores case
     * and anything that isn't a letter or a digit). Call it once per code entered: a second start
     * replaces the server id the first one is confirmed with.
     *
     * @param onStarted called on the render thread with the account the code belongs to
     * @param onFailed  called on the render thread if the API rejects the code or can't be reached
     */
    public static void start(String code, Consumer<Start> onStarted, Consumer<Failure> onFailed) {
        ConnectionHelper.runInBackground("account link", () -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("code", code);
                StartResponse response = ConnectionHelper.post("/api/minecraft-link/start", body, StartResponse.class);
                if (response == null || response.serverId == null || response.account == null || response.account.displayName == null) {
                    throw new IOException("The API responded to /start without a server id or an account");
                }
                Start start = new Start(code, response.serverId, response.account.displayName);
                ConnectionHelper.onRenderThread(() -> onStarted.accept(start));
            } catch (IOException | RuntimeException e) {
                fail(e, false, onFailed);
            }
        });
    }

    /**
     * Joins the server id of {@code start} as the signed-in player, then right away (Mojang keeps
     * the join for a few seconds only) asks the API to check it. Once linked, the game is signed in
     * to the Mo' Bends account ({@link MoBendsAccount#signIn}) and the player's accessories are
     * fetched again, even if the screen that asked has closed meanwhile: the code is spent either
     * way. Call it once per {@link Start}: of several confirmations, only one can succeed.
     *
     * @param onLinked called on the render thread, after signing in
     * @param onFailed called on the render thread if Mojang or the API refused, or can't be reached
     */
    public static void confirm(Start start, Consumer<Result> onLinked, Consumer<Failure> onFailed) {
        Minecraft mc = Minecraft.getInstance();
        User user = mc.getUser();
        ConnectionHelper.runInBackground("account link", () -> {
            try {
                //? if >=1.21 {
                mc.getMinecraftSessionService().joinServer(user.getProfileId(), user.getAccessToken(), start.serverId());
                //?} else {
                /*mc.getMinecraftSessionService().joinServer(user.getGameProfile(), user.getAccessToken(), start.serverId());
                *///?}

                JsonObject body = new JsonObject();
                body.addProperty("code", start.code());
                body.addProperty("username", user.getName());
                ConfirmResponse response = ConnectionHelper.post("/api/minecraft-link/confirm", body, ConfirmResponse.class);
                if (response == null || response.username == null) {
                    throw new IOException("The API responded to /confirm without a username");
                }
                String displayName = response.account != null && response.account.displayName != null
                    ? response.account.displayName : start.displayName();
                Result result = new Result(response.uuid, response.username, displayName);
                ConnectionHelper.onRenderThread(() -> {
                    MoBendsAccount.signIn(displayName, response.token);
                    // The player's accessories follow the account now.
                    SupporterContent.forgetPlayer(user.getName());
                    onLinked.accept(result);
                });
            } catch (AuthenticationException | IOException | RuntimeException e) {
                fail(e, true, onFailed);
            }
        });
    }

    private static void fail(Exception e, boolean confirming, Consumer<Failure> onFailed) {
        MoBends.LOGGER.warn("Couldn't link the Minecraft account: {}", e.toString());
        Failure failure = Failure.of(e, confirming);
        ConnectionHelper.onRenderThread(() -> onFailed.accept(failure));
    }

    private static final class StartResponse {
        String serverId;
        Account account;
    }

    private static final class ConfirmResponse {
        String uuid;
        String username;
        /** Null if the API hands out no tokens: the game is then signed in by name only. */
        @Nullable
        String token;
        @Nullable
        Account account;
    }

    private static final class Account {
        String displayName;
    }
}
