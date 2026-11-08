package goblinbob.mobends.core.client;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * What a mutation changes about a renderer: the model's part fields, the contents of its part
 * arrays and lists, and the renderer's layers. One renderer draws every entity of a kind (every
 * player is drawn by one of two), and entities that share it can have different types, so a
 * renderer's vanilla state and each of its mutations are captured once and put back in place
 * before every render, instead of mutating and demutating.
 */
public final class RendererState
{

    private static final Map<RenderLivingBase<?>, RendererState> VANILLA = new WeakHashMap<>();
    /** Renderers that currently hold a mutation. */
    private static final Map<RenderLivingBase<?>, Boolean> MUTATED = new WeakHashMap<>();

    private final ModelState model;
    private final List<LayerRenderer<?>> layers;

    private RendererState(RenderLivingBase<?> renderer)
    {
        this.model = ModelState.capture(renderer.getMainModel());
        this.layers = new ArrayList<>(renderer.layerRenderers);
    }

    /** The renderer's state as it is now. */
    public static RendererState capture(RenderLivingBase<?> renderer)
    {
        return new RendererState(renderer);
    }

    /** Puts the renderer back to vanilla, if a mutation is in place. */
    public static void restoreVanilla(RenderLivingBase<?> renderer)
    {
        if (MUTATED.remove(renderer) != null)
        {
            vanillaOf(renderer).apply(renderer);
        }
    }

    /**
     * The renderer's vanilla state, captured the first time it is asked for. Every mutation goes
     * through {@link #restoreVanilla} first, so this is always seen before the first mutation.
     */
    public static RendererState vanillaOf(RenderLivingBase<?> renderer)
    {
        return VANILLA.computeIfAbsent(renderer, RendererState::new);
    }

    /** Puts this mutation in place on the renderer. */
    public void applyMutation(RenderLivingBase<?> renderer)
    {
        vanillaOf(renderer);
        apply(renderer);
        MUTATED.put(renderer, Boolean.TRUE);
    }

    /** Marks the renderer as mutated, after a mutator changed it directly. */
    public static void markMutated(RenderLivingBase<?> renderer)
    {
        MUTATED.put(renderer, Boolean.TRUE);
    }

    @SuppressWarnings("unchecked")
    private void apply(RenderLivingBase<?> renderer)
    {
        if (renderer.getMainModel() != model.getModel())
        {
            // Another mod replaced the model; the captured parts no longer belong to it.
            return;
        }
        model.apply();
        List<LayerRenderer<?>> rendererLayers = (List<LayerRenderer<?>>) (Object) renderer.layerRenderers;
        rendererLayers.clear();
        rendererLayers.addAll(layers);
    }

}
