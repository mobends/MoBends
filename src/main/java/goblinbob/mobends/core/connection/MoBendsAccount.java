package goblinbob.mobends.core.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.util.ApiErrorException;
import goblinbob.mobends.core.util.ConnectionHelper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * The Mo' Bends account the game is signed in to, through a Minecraft account linked to it
 * ({@link MinecraftLink}); 1.12's {@code MoBendsAccount}. Linking hands the game a session token,
 * kept in {@code config/mobends/account.json} (readable by the player only) for each Minecraft
 * account the game is played with, so another account on the same game isn't signed in to it.
 * <p>
 * The session is checked with the API as the Mo' Bends menu opens ({@link #verify}): the player may
 * have unlinked the account on the dashboard, or linked it to another Mo' Bends account. Logging
 * out revokes the token. Render thread only, but for the requests, which run in the background.
 */
public final class MoBendsAccount {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Nullable
    private static Path file;
    /** The sessions by the undashed UUID of the Minecraft account they were linked with. */
    private static final Map<String, Session> sessions = new HashMap<>();

    private MoBendsAccount() {
    }

    /**
     * A Mo' Bends account the game is signed in to.
     *
     * @param token null if the API that linked the account handed out no token: such a session can't be checked
     */
    public record Session(String displayName, @Nullable String token) {
    }

    /** Reads {@code config/mobends/account.json}. Once, at start-up. */
    public static void init(Path configDir) {
        file = configDir.resolve(MoBends.MOD_ID).resolve("account.json");
        load();
    }

    /** The Mo' Bends account the current Minecraft account is signed in to, or null. */
    @Nullable
    public static Session getCurrent() {
        return sessions.get(currentMinecraftUuid());
    }

    /** Signs the current Minecraft account in, once it's been linked ({@link MinecraftLink#confirm} does). */
    public static void signIn(String displayName, @Nullable String token) {
        sessions.put(currentMinecraftUuid(), new Session(displayName, token));
        save();
    }

    /**
     * Signs the current Minecraft account out, and revokes its token in the background. It's
     * signed out even if the API can't be reached.
     */
    public static void logOut() {
        Session session = sessions.remove(currentMinecraftUuid());
        if (session == null) {
            return;
        }
        save();
        if (session.token() == null) {
            return;
        }
        ConnectionHelper.runInBackground("account", () -> {
            try {
                ConnectionHelper.sendAuthorized("DELETE", "/api/minecraft-link/session", session.token(), Void.class);
            } catch (IOException e) {
                MoBends.LOGGER.warn("Couldn't revoke the Mo' Bends session: {}", e.toString());
            }
        });
    }

    /**
     * Checks the current session with the API in the background: it's signed out if the API no
     * longer knows its token (401), and takes the account's current name otherwise; any other
     * error keeps the session. If either changes anything, {@code onChange} runs on the render
     * thread. Does nothing if the game isn't signed in, or by name only.
     */
    public static void verify(Runnable onChange) {
        String minecraftUuid = currentMinecraftUuid();
        Session session = sessions.get(minecraftUuid);
        if (session == null || session.token() == null) {
            return;
        }
        ConnectionHelper.runInBackground("account", () -> {
            Session replacement;
            try {
                SessionResponse response = ConnectionHelper.sendAuthorized("GET", "/api/minecraft-link/session", session.token(), SessionResponse.class);
                String displayName = response == null || response.account == null ? null : response.account.displayName;
                if (displayName == null || displayName.equals(session.displayName())) {
                    return;
                }
                replacement = new Session(displayName, session.token());
            } catch (ApiErrorException e) {
                // Only an unknown token signs out; anything else may pass.
                if (e.getStatus() != 401) {
                    MoBends.LOGGER.warn("Couldn't check the Mo' Bends session: {}", e.toString());
                    return;
                }
                replacement = null;
            } catch (IOException | RuntimeException e) {
                MoBends.LOGGER.warn("Couldn't check the Mo' Bends session: {}", e.toString());
                return;
            }
            Session newSession = replacement;
            ConnectionHelper.onRenderThread(() -> {
                // The player may have logged out or in meanwhile.
                if (sessions.get(minecraftUuid) != session) {
                    return;
                }
                if (newSession == null) {
                    sessions.remove(minecraftUuid);
                } else {
                    sessions.put(minecraftUuid, newSession);
                }
                save();
                onChange.run();
            });
        });
    }

    private static String currentMinecraftUuid() {
        UUID id = Minecraft.getInstance().getUser().getProfileId();
        return id.toString().replace("-", "").toLowerCase(Locale.ROOT);
    }

    private static void load() {
        if (file == null || !Files.isRegularFile(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            AccountFile contents = GSON.fromJson(reader, AccountFile.class);
            sessions.clear();
            if (contents != null && contents.sessions != null) {
                contents.sessions.forEach((uuid, session) -> {
                    if (uuid != null && session != null && session.displayName() != null) {
                        sessions.put(uuid, session);
                    }
                });
            }
        } catch (IOException | JsonParseException e) {
            MoBends.LOGGER.warn("Couldn't read {}: {}", file, e.toString());
        }
    }

    private static void save() {
        if (file == null) {
            return;
        }
        AccountFile contents = new AccountFile();
        contents.sessions = new HashMap<>(sessions);
        // Written aside and moved over the file, so a game closed mid-write doesn't lose every session.
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            Files.deleteIfExists(temporary);
            createPrivately(temporary);
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(contents, writer);
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            MoBends.LOGGER.warn("Couldn't save {}: {}", file, e.toString());
        }
    }

    /** Creates {@code path} readable and writable by its owner alone: the tokens are the player's. */
    private static void createPrivately(Path path) throws IOException {
        try {
            Files.createFile(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        } catch (UnsupportedOperationException e) {
            Files.createFile(path);
            java.io.File asFile = path.toFile();
            asFile.setReadable(false, false);
            asFile.setReadable(true, true);
            asFile.setWritable(false, false);
            asFile.setWritable(true, true);
        }
    }

    private static final class AccountFile {
        Map<String, Session> sessions;
    }

    private static final class SessionResponse {
        Account account;
    }

    private static final class Account {
        String displayName;
    }
}
