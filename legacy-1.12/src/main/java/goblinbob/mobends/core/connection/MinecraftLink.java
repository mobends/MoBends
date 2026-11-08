package goblinbob.mobends.core.connection;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.util.ConnectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;

/**
 * Links the player's Minecraft account to their Mo' Bends account, proving they own it the way an online-mode
 * server does: the API hands out a server id for the code from the dashboard, the game joins that "server" on
 * Mojang's session server, and the API checks the join with Mojang. The access token never leaves the game.
 * <p>
 * Every call blocks, so call them off the main thread. {@link #joinAndConfirm} must not run while the game connects
 * to a server: a join in the middle of that can make the connection fail with "Failed to verify username".
 */
public final class MinecraftLink
{

    private static final int TIMEOUT_MS = 10000;

    private MinecraftLink()
    {
    }

    /**
     * Starts linking with the code from the dashboard, as the player typed it (the API ignores case and anything
     * that isn't a letter or a digit).
     *
     * @throws goblinbob.mobends.core.util.ApiErrorException If the API rejects the code.
     */
    public static Start start(String code) throws IOException
    {
        JsonObject body = new JsonObject();
        body.addProperty("code", code);

        Start start = ConnectionHelper.sendPostRequest(endpoint("start"), body, Start.class, TIMEOUT_MS);
        if (start == null || start.serverId == null || start.account == null || start.account.displayName == null)
        {
            throw new IOException("The API responded to /start without a server id or an account");
        }
        return start;
    }

    /**
     * Joins the server id from {@link #start} as the signed-in player, then right away (Mojang keeps the join for a
     * few seconds only) asks the API to check it.
     *
     * @throws AuthenticationException If Mojang rejects the session, e.g. in an offline or development launch.
     * @throws goblinbob.mobends.core.util.ApiErrorException If the API couldn't verify the join.
     */
    public static Result joinAndConfirm(String code, Start start) throws AuthenticationException, IOException
    {
        Minecraft mc = Minecraft.getMinecraft();
        Session session = mc.getSession();
        GameProfile profile = session.getProfile();
        if (profile.getId() == null)
        {
            // An offline launch, whose player id isn't a UUID: authlib can't even form the request.
            throw new AuthenticationException("The session has no profile id");
        }

        mc.getSessionService().joinServer(profile, session.getToken(), start.serverId);

        JsonObject body = new JsonObject();
        body.addProperty("code", code);
        body.addProperty("username", session.getUsername());

        Result result = ConnectionHelper.sendPostRequest(endpoint("confirm"), body, Result.class, TIMEOUT_MS);
        if (result == null || result.username == null)
        {
            throw new IOException("The API responded to /confirm without a username");
        }
        return result;
    }

    private static URL endpoint(String name) throws IOException
    {
        return new URL(EnvironmentModule.getConfig().getApiUrl() + "/api/minecraft-link/" + name);
    }

    public static class Start
    {
        private String serverId;
        private Account account;

        public String getDisplayName()
        {
            return account.displayName;
        }
    }

    private static class Account
    {
        private String displayName;
    }

    public static class Result
    {
        private String uuid;
        private String username;
        private String token;
        private Account account;

        /** The player's UUID, undashed, as Mojang verified it. */
        public String getUuid()
        {
            return uuid;
        }

        /** The player's username, as Mojang spells it. */
        public String getUsername()
        {
            return username;
        }

        /** The token the game is signed in to the Mo' Bends account with, or null if the API hands out none. */
        @Nullable
        public String getToken()
        {
            return token;
        }

        /** The name of the Mo' Bends account the Minecraft account is linked to, or null if the API leaves it out. */
        @Nullable
        public String getDisplayName()
        {
            return account == null ? null : account.displayName;
        }
    }

}
