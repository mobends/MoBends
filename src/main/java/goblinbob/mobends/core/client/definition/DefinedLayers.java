package goblinbob.mobends.core.client.definition;

import com.google.gson.JsonObject;
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
        /**
         * The layer for {@code renderer}, configured by the definition's {@code options} for it;
         * {@code bones} finds the mutated part of a bone by name (null if there is none).
         */
        LayerRenderer<?> create(RenderLivingBase<?> renderer, JsonObject options, Function<String, ModelRenderer> bones);
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
