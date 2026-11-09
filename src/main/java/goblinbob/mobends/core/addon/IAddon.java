package goblinbob.mobends.core.addon;

/**
 * A mod's addon to Mo' Bends: the animated mobs, layers, components, drivers and KUMO operations it
 * brings, and the callbacks it wants. Found on the client at start-up, before any animator, model
 * definition or type file loads: on Fabric, through the {@code "mobends"} entrypoint of the mod's
 * {@code fabric.mod.json}; on NeoForge, through {@link MoBendsAddon} on the class. See
 * docs/addons.md. The callbacks run on the client's thread.
 *
 * @author Iwo Plaza
 */
public interface IAddon {
    /** Registers what the addon brings, once, before any animator loads. */
    void registerContent(AddonAnimationRegistry registry);

    /** The addon's name, for the log. */
    String getDisplayName();

    /**
     * Called on each rendered frame in a world, unless the game is paused, after the animated
     * entities are updated.
     *
     * @param partialTicks a value between 0 and 1 describing how close we are to the next client tick
     */
    default void onRenderTick(float partialTicks) {
        // No default behaviour
    }

    /** Called on each client tick in a world, unless the game is paused, after the animated entities are updated. */
    default void onClientTick() {
        // No default behaviour
    }

    /**
     * Called when the animation is refreshed: on every resource reload, the Mo' Bends menu's Refresh
     * and a change to its settings. This is where all cached resources should be freed.
     */
    default void onRefresh() {
        // No default behaviour
    }
}
