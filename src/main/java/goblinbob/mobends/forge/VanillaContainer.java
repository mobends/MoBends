package goblinbob.mobends.forge;

import net.minecraft.client.renderer.model.ModelRenderer;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class VanillaContainer
{
    private final List<Entry> entries = new LinkedList<>();

    public void store(Field field, ModelRenderer originalPart)
    {
        this.entries.add(new Entry(field, originalPart));
    }

    public Iterable<Entry> getEntries()
    {
        return entries;
    }

    public void clear()
    {
        entries.clear();
    }

    public static class Entry
    {
        public final Field field;
        public final ModelRenderer originalPart;

        public Entry(Field field, ModelRenderer originalPart)
        {
            this.field = field;
            this.originalPart = originalPart;
        }
    }
}
