package goblinbob.mobends.core.client.definition;

import com.google.gson.JsonObject;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The renderer layers a model definition can ask for in its {@code layers} section, by id
 * ({@code mobends:armor}). A layer either replaces the renderer's vanilla layer of a class (which
 * draws its own unanimated copy of the model) or is added after the renderer's own. Addons add
 * theirs with {@code AddonAnimationRegistry.registerLayer}.
 */
public final class DefinedLayers
{

    @FunctionalInterface
    public interface Factory
    {
        /** The layer for the context's renderer. */
        LayerRenderer<?> create(Context context);
    }

    /** What a layer is made with. */
    public static final class Context
    {
        public final RenderLivingBase<?> renderer;
        /** The definition's options for the layer. */
        public final JsonObject options;
        /** Finds the mutated part of a bone by name (null if there is none). */
        public final Function<String, ModelRenderer> bones;
        /** The model definition asking for the layer. */
        public final EntityModelDefinition definition;
        /** The vanilla layer it replaces; null for a layer added after the renderer's own. */
        @Nullable
        public final LayerRenderer<?> replaced;

        public Context(RenderLivingBase<?> renderer, JsonObject options, Function<String, ModelRenderer> bones, EntityModelDefinition definition,
                       @Nullable LayerRenderer<?> replaced)
        {
            this.renderer = renderer;
            this.options = options;
            this.bones = bones;
            this.definition = definition;
            this.replaced = replaced;
        }
    }

    public static final class Kind
    {
        /** The vanilla layer class it replaces; null for a layer added after the renderer's own. */
        @Nullable
        public final Class<?> replaces;
        public final Factory factory;

        Kind(@Nullable Class<?> replaces, Factory factory)
        {
            this.replaces = replaces;
            this.factory = factory;
        }
    }

    private static final Map<String, Kind> KINDS = new HashMap<>();

    private DefinedLayers()
    {
    }

    public static void register(String id, @Nullable Class<?> replaces, Factory factory)
    {
        KINDS.put(id, new Kind(replaces, factory));
    }

    @Nullable
    public static Kind get(String id)
    {
        return KINDS.get(id);
    }

}
