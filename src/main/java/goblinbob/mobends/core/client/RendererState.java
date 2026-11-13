package goblinbob.mobends.core.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

import java.util.*;

/**
 * What a mutation changes about a renderer: the model's part fields, the contents of its part
 * arrays and lists, and the renderer's layers. One renderer draws every entity of a kind (every
 * player is drawn by one of two), and entities that share it can have different types, so a
 * renderer's vanilla state and each of its mutations are captured once and put back in place
 * before every render, instead of mutating and demutating.
 * <p>
 * A renderer may draw with several models, swapping its main model and layers before each entity (1.8.9's
 * zombie renderer draws zombie villagers with their own model), so the state is kept per main model too.
 */
public final class RendererState
{

    private static final Map<RendererLivingEntity<?>, Map<ModelBase, RendererState>> VANILLA = new WeakHashMap<>();
    /** The main models of each renderer that currently hold a mutation. */
    private static final Map<RendererLivingEntity<?>, Set<ModelBase>> MUTATED = new WeakHashMap<>();

    private final ModelState model;
    private final List<LayerRenderer<?>> layers;

    private RendererState(RendererLivingEntity<?> renderer)
    {
        this.model = ModelState.capture(renderer.getMainModel());
        this.layers = new ArrayList<>(renderer.layerRenderers);
    }

    /** The renderer's state as it is now. */
    public static RendererState capture(RendererLivingEntity<?> renderer)
    {
        return new RendererState(renderer);
    }

    /** Puts the renderer (with its current main model) back to vanilla, if a mutation is in place. */
    public static void restoreVanilla(RendererLivingEntity<?> renderer)
    {
        final Set<ModelBase> mutated = MUTATED.get(renderer);
        if (mutated != null && mutated.remove(renderer.getMainModel()))
        {
            vanillaOf(renderer).apply(renderer);
        }
    }

    /**
     * The renderer's vanilla state with its current main model, captured the first time it is asked for. Every
     * mutation goes through {@link #restoreVanilla} first, so this is always seen before the first mutation.
     */
    public static RendererState vanillaOf(RendererLivingEntity<?> renderer)
    {
        return VANILLA.computeIfAbsent(renderer, r -> new IdentityHashMap<>())
                .computeIfAbsent(renderer.getMainModel(), m -> new RendererState(renderer));
    }

    /** Puts this mutation in place on the renderer. */
    public void applyMutation(RendererLivingEntity<?> renderer)
    {
        vanillaOf(renderer);
        apply(renderer);
        markMutated(renderer);
    }

    /** Marks the renderer (with its current main model) as mutated, after a mutator changed it directly. */
    public static void markMutated(RendererLivingEntity<?> renderer)
    {
        MUTATED.computeIfAbsent(renderer, r -> Collections.newSetFromMap(new IdentityHashMap<>())).add(renderer.getMainModel());
    }

    @SuppressWarnings("unchecked")
    private void apply(RendererLivingEntity<?> renderer)
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
