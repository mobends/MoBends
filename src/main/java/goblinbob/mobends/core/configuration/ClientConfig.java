package goblinbob.mobends.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.kumo.option.KumoOptions;
import goblinbob.mobends.core.util.WildcardPattern;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.UseActionType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * The client's settings, in {@code config/mobends.json} (see docs/config.md): what 1.12 kept in
 * {@code config/mobends.cfg}, Forge's {@code @Config} settings ({@code ModConfig}) and the Mo' Bends
 * menu's ({@code CoreClientConfig}) alike. It's the mod's own file, read and written the same way on
 * every loader. A file that can't be read is left alone, and the defaults are used until the next save.
 * Render thread only.
 */
public final class ClientConfig {
    public static final ClientConfig INSTANCE = new ClientConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String FILE_NAME = "mobends.json";

    /** What the file holds; its fields are its keys. */
    static final class Values {
        boolean showSwordTrail = true;
        boolean showArrowTrails = true;
        /** {@code <item pattern>=<FOOD|BOW|SHIELD>}: how items matching the pattern are used, in place of how their kind is. */
        List<String> itemUseClassifications = new ArrayList<>();
        /** {@code <item pattern>=<FISTS|SWORD|TOOL>}: how items matching the pattern attack. */
        List<String> itemAttackClassifications = new ArrayList<>();
        /** Item patterns: armour that stays as vanilla draws it on a bent entity. */
        List<String> keepArmorAsVanilla = new ArrayList<>();
        /** Entity type patterns: entities that stay vanilla, whatever their type says. */
        List<String> keepEntityAsVanilla = new ArrayList<>();
        /** Per animated mob (its key, {@code mobends:player}): whether it's animated; absent means it is. */
        Map<String, Boolean> animated = new TreeMap<>();
        /** The order the user gave the entity types, by type id; absent means 0. */
        Map<String, Integer> typeRanks = new TreeMap<>();
        /** The order the user gave the extensions, by extension id; absent means 0. */
        Map<String, Integer> extensionRanks = new TreeMap<>();
        /** The values the user gave options, by key ({@code <owner>#<name>}); absent means the default. */
        Map<String, Double> options = new TreeMap<>();
    }

    private Values values = new Values();
    @Nullable
    private Path file;
    /** The classifications, parsed when first needed after a load. */
    @Nullable
    private Map<String, UseActionType> useClassifications;
    @Nullable
    private Map<String, AttackActionType> attackClassifications;

    private ClientConfig() {
    }

    /** Reads the file (or writes the defaults, the first time), and hands the options their values. */
    public void load(Path configDir) {
        file = configDir.resolve(FILE_NAME);
        values = new Values();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                Values read = GSON.fromJson(reader, Values.class);
                if (read != null) {
                    values = withDefaults(read);
                }
            } catch (IOException | JsonParseException e) {
                MoBends.LOGGER.error("Could not read {}; the defaults are used until the settings are saved again.", file, e);
                file = null;
            }
        } else {
            save();
        }
        useClassifications = null;
        attackClassifications = null;
        KumoOptions.INSTANCE.load(values.options);
        KumoOptions.INSTANCE.setListener(this::setOptionValue);
    }

    /** Gson leaves out what the file doesn't have; those keep their defaults. */
    private static Values withDefaults(Values read) {
        Values defaults = new Values();
        if (read.itemUseClassifications == null) read.itemUseClassifications = defaults.itemUseClassifications;
        if (read.itemAttackClassifications == null) read.itemAttackClassifications = defaults.itemAttackClassifications;
        if (read.keepArmorAsVanilla == null) read.keepArmorAsVanilla = defaults.keepArmorAsVanilla;
        if (read.keepEntityAsVanilla == null) read.keepEntityAsVanilla = defaults.keepEntityAsVanilla;
        read.animated = read.animated == null ? defaults.animated : new TreeMap<>(read.animated);
        read.typeRanks = read.typeRanks == null ? defaults.typeRanks : new TreeMap<>(read.typeRanks);
        read.extensionRanks = read.extensionRanks == null ? defaults.extensionRanks : new TreeMap<>(read.extensionRanks);
        read.options = read.options == null ? defaults.options : new TreeMap<>(read.options);
        return read;
    }

    /** Writes the settings, through a temporary file so a crash never leaves half a file. */
    public void save() {
        if (file == null) {
            // The file couldn't be read: it's left as the user has it.
            return;
        }
        try {
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(FILE_NAME + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(values, writer);
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            MoBends.LOGGER.error("Could not save {}", file, e);
        }
    }

    // --- trails -------------------------------------------------------------------------------------

    public boolean showSwordTrail() {
        return values.showSwordTrail;
    }

    public void setShowSwordTrail(boolean show) {
        values.showSwordTrail = show;
        save();
    }

    public boolean showArrowTrails() {
        return values.showArrowTrails;
    }

    public void setShowArrowTrails(boolean show) {
        values.showArrowTrails = show;
        save();
    }

    // --- the lists of patterns ----------------------------------------------------------------------

    /** The lists of patterns the file holds, as the Mo' Bends menu's More Settings edits them. */
    public enum PatternList {
        ITEM_USE_CLASSIFICATIONS,
        ITEM_ATTACK_CLASSIFICATIONS,
        KEEP_ARMOR_AS_VANILLA,
        KEEP_ENTITY_AS_VANILLA
    }

    /** The entries of a list, in order (a copy). */
    public List<String> patterns(PatternList list) {
        return new ArrayList<>(listOf(list));
    }

    /** Replaces the entries of a list, and saves. */
    public void setPatterns(PatternList list, List<String> entries) {
        List<String> kept = listOf(list);
        kept.clear();
        kept.addAll(entries);
        useClassifications = null;
        attackClassifications = null;
        save();
    }

    private List<String> listOf(PatternList list) {
        return switch (list) {
            case ITEM_USE_CLASSIFICATIONS -> values.itemUseClassifications;
            case ITEM_ATTACK_CLASSIFICATIONS -> values.itemAttackClassifications;
            case KEEP_ARMOR_AS_VANILLA -> values.keepArmorAsVanilla;
            case KEEP_ENTITY_AS_VANILLA -> values.keepEntityAsVanilla;
        };
    }

    // --- what's animated ----------------------------------------------------------------------------

    /** Whether the mob with this key ({@code mobends:player}) is animated. */
    public boolean isAnimated(String key) {
        return values.animated.getOrDefault(key, true);
    }

    public void setAnimated(String key, boolean animated) {
        if (animated) {
            values.animated.remove(key);
        } else {
            values.animated.put(key, false);
        }
        save();
    }

    /** Whether an entity of this type ({@code minecraft:zombie}) stays vanilla, whatever its type says. */
    public boolean keepsEntityVanilla(String entityType) {
        return matchesAny(entityType, values.keepEntityAsVanilla);
    }

    /** Whether this armour item ({@code minecraft:iron_chestplate}) stays as vanilla draws it on a bent entity. */
    public boolean keepsArmorVanilla(String item) {
        return matchesAny(item, values.keepArmorAsVanilla);
    }

    private static boolean matchesAny(String location, List<String> patterns) {
        for (String pattern : patterns) {
            if (WildcardPattern.matchesLocation(location, pattern)) {
                return true;
            }
        }
        return false;
    }

    // --- item classifications -----------------------------------------------------------------------

    /** How the user says this item ({@code minecraft:spyglass}) is used, or null if they don't. */
    @Nullable
    public UseActionType useActionOf(String item) {
        if (useClassifications == null) {
            useClassifications = parseClassifications(values.itemUseClassifications, UseActionType.class, "itemUseClassifications");
        }
        return classify(item, useClassifications);
    }

    /** How the user says this item attacks, or null if they don't. */
    @Nullable
    public AttackActionType attackActionOf(String item) {
        if (attackClassifications == null) {
            attackClassifications = parseClassifications(values.itemAttackClassifications, AttackActionType.class, "itemAttackClassifications");
        }
        return classify(item, attackClassifications);
    }

    /** The last entry whose pattern matches wins, as in 1.12. */
    @Nullable
    private static <T> T classify(String item, Map<String, T> classifications) {
        T found = null;
        for (Map.Entry<String, T> entry : classifications.entrySet()) {
            if (WildcardPattern.matchesLocation(item, entry.getKey())) {
                found = entry.getValue();
            }
        }
        return found;
    }

    /** {@code <pattern>=<action>} entries, in order; malformed ones are said and left out. */
    private static <T extends Enum<T>> Map<String, T> parseClassifications(List<String> entries, Class<T> type, String key) {
        Map<String, T> parsed = new LinkedHashMap<>();
        for (String entry : entries) {
            int equals = entry.indexOf('=');
            if (equals < 0) {
                MoBends.LOGGER.warn("{} in {}: '{}' has no '=' (write <item pattern>=<action>); it is left out.", key, FILE_NAME, entry);
                continue;
            }
            String action = entry.substring(equals + 1).trim().toUpperCase(Locale.ROOT);
            try {
                parsed.put(entry.substring(0, equals).trim(), Enum.valueOf(type, action));
            } catch (IllegalArgumentException e) {
                MoBends.LOGGER.warn("{} in {}: '{}' isn't an action; it is left out.", key, FILE_NAME, action);
            }
        }
        return parsed;
    }

    // --- the order of types and extensions ----------------------------------------------------------

    public int typeRank(String typeId) {
        return values.typeRanks.getOrDefault(typeId, 0);
    }

    /** Sets a type's rank; 0 removes it. */
    public void setTypeRank(String typeId, int rank) {
        if (rank == 0) values.typeRanks.remove(typeId);
        else values.typeRanks.put(typeId, rank);
        save();
    }

    public int extensionRank(String extensionId) {
        return values.extensionRanks.getOrDefault(extensionId, 0);
    }

    /** Sets an extension's rank; 0 removes it. */
    public void setExtensionRank(String extensionId, int rank) {
        if (rank == 0) values.extensionRanks.remove(extensionId);
        else values.extensionRanks.put(extensionId, rank);
        save();
    }

    // --- options ------------------------------------------------------------------------------------

    /** Stores the value the user gave an option; null, its default, removes it (the options' listener). */
    private void setOptionValue(String key, @Nullable Double value) {
        if (value == null) {
            values.options.remove(key);
        } else {
            values.options.put(key, value);
        }
        save();
    }
}
