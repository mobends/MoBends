package goblinbob.mobends.core.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tells the player in chat, besides the log, what went wrong with Mo' Bends: an animator or an
 * extension that can't load or stops working, the API join failing, a malformed entry in the
 * config. A yellow "[Mo' Bends]" before it, as 1.12 had it.
 * <p>
 * Each notice has a key and is shown once, until {@link #forget} lets it be shown again (after a
 * reload). Reported while there's no player to tell (on the title screen, or while the config loads
 * at start-up), it waits for one: {@link #flush} shows it at the next client tick in a world. Any
 * thread may report; notices are shown on the render thread.
 */
public final class ErrorReporter {
    /** The keys shown, until forgotten. */
    private static final Set<String> SHOWN = new HashSet<>();
    /** What's waiting for a player, by key, in the order reported. */
    private static final Map<String, String> PENDING = new LinkedHashMap<>();

    private ErrorReporter() {
    }

    /** Shows {@code message} in chat at the next client tick in a world, unless {@code key} was shown already. */
    public static void report(String key, String message) {
        synchronized (SHOWN) {
            if (!SHOWN.contains(key)) {
                PENDING.putIfAbsent(key, message);
            }
        }
    }

    /** Lets the notices whose keys start with {@code prefix} be shown again (and drops those waiting). */
    public static void forget(String prefix) {
        synchronized (SHOWN) {
            SHOWN.removeIf(key -> key.startsWith(prefix));
            PENDING.keySet().removeIf(key -> key.startsWith(prefix));
        }
    }

    /** Shows what's waiting, if there's a player to tell. Render thread, at the start of a client tick. */
    public static void flush() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        Map<String, String> due;
        synchronized (SHOWN) {
            if (PENDING.isEmpty()) {
                return;
            }
            due = new LinkedHashMap<>(PENDING);
            PENDING.clear();
            SHOWN.addAll(due.keySet());
        }
        for (String message : due.values()) {
            minecraft.gui.getChat().addMessage(Component.literal("[Mo' Bends] ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(message).withStyle(ChatFormatting.WHITE)));
        }
    }
}
