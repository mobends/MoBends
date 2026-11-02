package goblinbob.mobends.core.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.module.IModule;
import goblinbob.mobends.core.util.ApiErrorException;
import goblinbob.mobends.core.util.ConnectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The Mo' Bends account the game is signed in to, through a Minecraft account linked to it (see {@link MinecraftLink}).
 * Linking hands the game a session token, kept in {@code config/mobends/account.json} for each Minecraft account the
 * game is played with, so another account on the same game isn't signed in to it.
 * <p>
 * The session is checked with the API now and then ({@link #verify}): the player may have unlinked the account on the
 * dashboard, or linked it to another Mo' Bends account. Logging out revokes the token.
 */
public final class MoBendsAccount
{

    private static final int TIMEOUT_MS = 10000;
    private static MoBendsAccount INSTANCE;

    private final File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    /** The sessions by the undashed UUID of the Minecraft account they were linked with. */
    private final Map<String, Session> sessions = new HashMap<>();

    private MoBendsAccount(File configDirectory)
    {
        this.file = new File(new File(configDirectory, "mobends"), "account.json");
        load();
    }

    /** The Mo' Bends account the current Minecraft account is signed in to, or null. */
    @Nullable
    public static Session getCurrent()
    {
        String minecraftUuid = currentMinecraftUuid();
        if (minecraftUuid == null)
        {
            return null;
        }

        synchronized (INSTANCE.sessions)
        {
            return INSTANCE.sessions.get(minecraftUuid);
        }
    }

    /** Signs the current Minecraft account in, once it's been linked. */
    public static void signIn(String displayName, @Nullable String token)
    {
        String minecraftUuid = currentMinecraftUuid();
        if (minecraftUuid == null)
        {
            return;
        }

        synchronized (INSTANCE.sessions)
        {
            INSTANCE.sessions.put(minecraftUuid, new Session(displayName, token));
        }
        INSTANCE.save();
    }

    /**
     * Signs the current Minecraft account out, and revokes its token on a thread of its own. It's signed out even if
     * the API can't be reached.
     */
    public static void logOut()
    {
        Session session = INSTANCE.forget(currentMinecraftUuid());
        if (session == null || session.token == null)
        {
            return;
        }

        runInBackground(() -> {
            try
            {
                ConnectionHelper.sendAuthorizedRequest("DELETE", sessionEndpoint(), session.token, Void.class, TIMEOUT_MS);
            }
            catch (IOException e)
            {
                Core.LOG.warning("Couldn't revoke the Mo' Bends session: " + e);
            }
        });
    }

    /**
     * Checks the current session with the API on a thread of its own: it's signed out if the API no longer knows its
     * token, and takes the account's current name otherwise. If either changes anything, then {@code onChange} runs on
     * the main thread.
     */
    public static void verify(Runnable onChange)
    {
        String minecraftUuid = currentMinecraftUuid();
        Session session = getCurrent();
        if (session == null || session.token == null)
        {
            return;
        }

        runInBackground(() -> {
            boolean changed;
            try
            {
                SessionResponse response = ConnectionHelper.sendAuthorizedRequest("GET", sessionEndpoint(), session.token,
                        SessionResponse.class, TIMEOUT_MS);
                String displayName = response == null || response.account == null ? null : response.account.displayName;
                changed = displayName != null && !displayName.equals(session.displayName)
                        && INSTANCE.replace(minecraftUuid, session, new Session(displayName, session.token));
            }
            catch (ApiErrorException e)
            {
                // Only an unknown token signs out; anything else may pass.
                changed = e.getStatus() == 401 && INSTANCE.replace(minecraftUuid, session, null);
            }
            catch (IOException | JsonParseException e)
            {
                Core.LOG.warning("Couldn't check the Mo' Bends session: " + e);
                changed = false;
            }

            if (changed)
            {
                Minecraft.getMinecraft().addScheduledTask(onChange);
            }
        });
    }

    @Nullable
    private static String currentMinecraftUuid()
    {
        String playerId = Minecraft.getMinecraft().getSession().getPlayerID();
        return playerId == null || playerId.isEmpty() ? null : playerId.replace("-", "").toLowerCase();
    }

    private static URL sessionEndpoint() throws IOException
    {
        return new URL(EnvironmentModule.getConfig().getApiUrl() + "/api/minecraft-link/session");
    }

    private static void runInBackground(Runnable task)
    {
        Thread thread = new Thread(task, "Mo' Bends account");
        thread.setDaemon(true);
        thread.start();
    }

    @Nullable
    private Session forget(@Nullable String minecraftUuid)
    {
        Session session;
        synchronized (sessions)
        {
            session = minecraftUuid == null ? null : sessions.remove(minecraftUuid);
        }
        if (session != null)
        {
            save();
        }
        return session;
    }

    /** Replaces the session, if it's still {@code expected} (the player may have logged out or in meanwhile). */
    private boolean replace(String minecraftUuid, Session expected, @Nullable Session replacement)
    {
        synchronized (sessions)
        {
            if (sessions.get(minecraftUuid) != expected)
            {
                return false;
            }
            if (replacement == null)
                sessions.remove(minecraftUuid);
            else
                sessions.put(minecraftUuid, replacement);
        }
        save();
        return true;
    }

    private void load()
    {
        if (!file.isFile())
        {
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))
        {
            AccountFile contents = gson.fromJson(reader, AccountFile.class);
            if (contents != null && contents.sessions != null)
            {
                synchronized (sessions)
                {
                    sessions.clear();
                    sessions.putAll(contents.sessions);
                }
            }
        }
        catch (IOException | JsonParseException e)
        {
            Core.LOG.warning("Couldn't read " + file + ": " + e);
        }
    }

    private void save()
    {
        AccountFile contents = new AccountFile();
        synchronized (sessions)
        {
            contents.sessions = new HashMap<>(sessions);
        }

        File directory = file.getParentFile();
        if (!directory.isDirectory() && !directory.mkdirs())
        {
            Core.LOG.warning("Couldn't create " + directory);
            return;
        }

        try
        {
            if (!file.exists() && file.createNewFile())
            {
                // The tokens are the player's alone.
                file.setReadable(false, false);
                file.setReadable(true, true);
                file.setWritable(false, false);
                file.setWritable(true, true);
            }
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))
            {
                gson.toJson(contents, writer);
            }
        }
        catch (IOException e)
        {
            Core.LOG.warning("Couldn't save " + file + ": " + e);
        }
    }

    /** A Mo' Bends account the game is signed in to. */
    public static final class Session
    {
        private final String displayName;
        /** Null if the API that linked the account didn't hand out tokens: such a session can't be checked. */
        @Nullable
        private final String token;

        Session(String displayName, @Nullable String token)
        {
            this.displayName = displayName;
            this.token = token;
        }

        public String getDisplayName()
        {
            return displayName;
        }
    }

    private static class AccountFile
    {
        Map<String, Session> sessions;
    }

    private static class SessionResponse
    {
        Account account;
    }

    private static class Account
    {
        String displayName;
    }

    public static class Factory implements IModule
    {
        @Override
        public void preInit(FMLPreInitializationEvent event)
        {
            INSTANCE = new MoBendsAccount(event.getModConfigurationDirectory());
        }

        @Override
        public void onRefresh()
        {
            INSTANCE.load();
        }
    }

}
