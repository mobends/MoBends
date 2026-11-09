package goblinbob.mobends.core.configuration;

import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.UseActionType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** LAB SHIM. The player's settings as a fresh config has them (no item classified, every rank 0), kept in memory. */
public final class ClientConfig {
    public static final ClientConfig INSTANCE = new ClientConfig();

    private final Map<String, Integer> extensionRanks = new HashMap<>();

    private ClientConfig() {
    }

    @Nullable
    public UseActionType useActionOf(String item) {
        return null;
    }

    @Nullable
    public AttackActionType attackActionOf(String item) {
        return null;
    }

    public int extensionRank(String extensionId) {
        return extensionRanks.getOrDefault(extensionId, 0);
    }

    /** Sets an extension's rank (the user moved it in the Mo' Bends menu); 0 removes it. Nothing is saved. */
    public void setExtensionRank(String extensionId, int rank) {
        if (rank == 0) {
            extensionRanks.remove(extensionId);
        } else {
            extensionRanks.put(extensionId, rank);
        }
    }
}
