package goblinbob.mobends.core.client.gui.settingswindow;

// The Mo' Bends menu isn't ported to 26.x's GUI yet.
//? if <26.1 {
import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.types.EntityType;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.types.Extension;
import goblinbob.mobends.core.types.TypeOrder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A list the user orders like resource packs: the types that can animate one kind of entity (when
 * several apply, the highest one wins), or their extensions (the highest one goes on top). Moving
 * an entry ranks the whole list, and Reset puts every rank back to 0. Every change applies at once
 * (the entities' animation starts over) and is saved.
 */
public class GuiOrderWindow<T extends TypeOrder.Ranked> extends GuiEntryWindow<GuiOrderEntry> {
    /** What is being ordered. */
    public interface Source<T> {
        Component getTitle();

        Component getHint();

        /** The entries, in their current order. */
        List<T> fetch();

        String describe(T entry);

        /** Ranks the entries in this order, the first one highest. */
        void setOrder(List<T> order);

        void reset(List<T> entries);
    }

    public static GuiOrderWindow<EntityType> forTypes(DefinedEntity defined, @Nullable Screen exit) {
        return new GuiOrderWindow<>(new Source<>() {
            @Override public Component getTitle() { return Component.translatable("mobends.gui.typeorder.title", GuiBenderSettings.nameOf(defined)); }
            @Override public Component getHint() { return Component.translatable("mobends.gui.typeorder.hint"); }
            @Override public List<EntityType> fetch() { return EntityTypeRegistry.INSTANCE.getTypesFor(defined); }
            @Override public void setOrder(List<EntityType> order) { EntityTypeRegistry.INSTANCE.setOrder(order); }
            @Override public void reset(List<EntityType> entries) { EntityTypeRegistry.INSTANCE.resetRanks(entries); }

            @Override
            public String describe(EntityType type) {
                String conditions = Component.translatable("mobends.gui.typeorder.conditions", type.getSpecificity()).getString();
                return type.getSource() + " · " + conditions + describeRank(type);
            }
        }, exit);
    }

    public static GuiOrderWindow<Extension> forExtensions(DefinedEntity defined, @Nullable Screen exit) {
        return new GuiOrderWindow<>(new Source<>() {
            @Override public Component getTitle() { return Component.translatable("mobends.gui.extensionorder.title", GuiBenderSettings.nameOf(defined)); }
            @Override public Component getHint() { return Component.translatable("mobends.gui.extensionorder.hint"); }
            @Override public List<Extension> fetch() { return EntityTypeRegistry.INSTANCE.getExtensionsFor(defined); }
            @Override public void setOrder(List<Extension> order) { EntityTypeRegistry.INSTANCE.setExtensionOrder(order); }
            @Override public void reset(List<Extension> entries) { EntityTypeRegistry.INSTANCE.resetExtensionRanks(entries); }

            @Override
            public String describe(Extension extension) {
                String extended = Component.translatable("mobends.gui.extensionorder.extends", extension.getTypeId()).getString();
                return extension.getSource() + " · " + extended + describeRank(extension);
            }
        }, exit);
    }

    private static String describeRank(TypeOrder.Ranked entry) {
        return entry.getRank() != 0 ? " · " + Component.translatable("mobends.gui.typeorder.rank", entry.getRank()).getString() : "";
    }

    private final Source<T> source;
    private List<T> entries;

    public GuiOrderWindow(Source<T> source, @Nullable Screen exit) {
        super(source.getTitle(), exit);
        this.source = source;
        fetchEntries();
    }

    @Override
    protected Component getHint() {
        return source.getHint();
    }

    @Override
    protected void reset() {
        source.reset(entries);
        fetchEntries();
    }

    private void move(int index, int offset) {
        int target = index + offset;
        if (target < 0 || target >= entries.size()) {
            return;
        }
        entries.add(target, entries.remove(index));
        source.setOrder(entries);
        fetchEntries();
    }

    private void fetchEntries() {
        entries = source.fetch();
        entryList.clearElements();
        for (int i = 0; i < entries.size(); i++) {
            int index = i;
            T entry = entries.get(i);
            entryList.addElement(new GuiOrderEntry(entry.getId(), source.describe(entry), i, entries.size(), () -> move(index, -1), () -> move(index, 1)));
        }
    }
}
//?}
