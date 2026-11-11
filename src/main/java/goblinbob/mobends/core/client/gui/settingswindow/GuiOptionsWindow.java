package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.kumo.option.KumoOption;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.types.EntityType;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.types.Extension;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The options of one kind of entity: those of its model definition, of the types that can animate
 * it and of their extensions. A change applies at once and is saved; Reset puts every one back to
 * its default.
 */
public class GuiOptionsWindow extends GuiEntryWindow<GuiOptionEntry> {
    /** An option, with where it comes from. */
    public static final class Entry {
        public final KumoOption option;
        public final String source;

        Entry(KumoOption option, String source) {
            this.option = option;
            this.source = source;
        }
    }

    /** The options of the entities {@code defined} animates: its model definition's, then its types' and their extensions', in precedence order. */
    public static List<Entry> optionsOf(DefinedEntity defined) {
        List<Entry> entries = new ArrayList<>();
        if (defined.key() != null) {
            add(entries, defined.definition().options(), Component.translatable("mobends.gui.options.model").getString());
        }
        for (EntityType type : EntityTypeRegistry.INSTANCE.getTypesFor(defined)) {
            add(entries, type.getOptions(), Component.translatable("mobends.gui.options.type", type.getId()).getString());
        }
        for (Extension extension : EntityTypeRegistry.INSTANCE.getExtensionsFor(defined)) {
            add(entries, extension.getOptions(), Component.translatable("mobends.gui.options.extension", extension.getId()).getString());
        }
        return entries;
    }

    private static void add(List<Entry> entries, OptionSet options, String source) {
        for (KumoOption option : options.all()) {
            entries.add(new Entry(option, source));
        }
    }

    private final List<Entry> entries;

    public GuiOptionsWindow(DefinedEntity defined, @Nullable Screen exit) {
        super(Component.translatable("mobends.gui.options.title", GuiBenderSettings.nameOf(defined)), exit);
        this.entries = optionsOf(defined);
        for (Entry entry : entries) {
            entryList.addElement(new GuiOptionEntry(entry.option, entry.source));
        }
    }

    @Override
    protected Component getHint() {
        return Component.translatable("mobends.gui.options.hint");
    }

    @Override
    protected void reset() {
        for (Entry entry : entries) {
            entry.option.reset();
        }
    }
}
