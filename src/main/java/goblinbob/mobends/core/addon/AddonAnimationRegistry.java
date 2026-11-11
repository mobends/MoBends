package goblinbob.mobends.core.addon;

import goblinbob.mobends.core.client.definition.DefinedLayers;
import goblinbob.mobends.core.data.EntityComponents;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.api.NumberFunctions;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.driver.IDriverFactory;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * What an addon registers its content with ({@link IAddon#registerContent}). Everything is named
 * without a namespace and registered as {@code "<modid>:<name>"}, the addon's mod id. Everything has
 * to be registered before the first animator loads; later, it throws.
 */
public class AddonAnimationRegistry {
    private final String modId;

    public AddonAnimationRegistry(String modId) {
        this.modId = modId;
    }

    /** The mod id everything is registered under. */
    public String getModId() {
        return modId;
    }

    /**
     * Registers an operation animators can use in their expressions, as "modid:name" (the
     * operation is named without the mod id, in snake_case). See {@link KumoOperation} for the
     * signature, and {@link #registerFunction} and {@link #registerEntityFloatReader} for the
     * shorter ways.
     */
    public void registerOperation(KumoOperation operation) {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerOperation(operation.renamed(namespaced(operation.name)));
    }

    /** Registers a pure function of numbers as "modid:key": computed once, when an animator loads, if its argument is written out. */
    public void registerFunction(String key, NumberFunctions.Unary function) {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerFunction(namespaced(key), function);
    }

    public void registerFunction(String key, NumberFunctions.Binary function) {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerFunction(namespaced(key), function);
    }

    public void registerFunction(String key, NumberFunctions.Ternary function) {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerFunction(namespaced(key), function);
    }

    /**
     * Registers a number read from the entity, as "modid:key": {@code {"mymod:wetness": []}}.
     * {@code type} is where it applies: an entity of another class takes the operation's
     * {@code @fallback}, or its animator fails to load.
     */
    public <E extends Entity> void registerEntityFloatReader(String key, Class<E> type, ToDoubleFunction<? super E> reader) {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerEntityFloatReader(namespaced(key), type, reader);
    }

    /**
     * Registers a double read from the entity, as "modid:key": a position in the world, say, which
     * a float would round; see {@link #registerEntityFloatReader}.
     */
    public <E extends Entity> void registerEntityDoubleReader(String key, Class<E> type, ToDoubleFunction<? super E> reader) {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerEntityDoubleReader(namespaced(key), type, reader);
    }

    /** Registers whether the entity is something, as "modid:key"; see {@link #registerEntityFloatReader}. */
    public <E extends Entity> void registerEntityBooleanReader(String key, Class<E> type, Predicate<? super E> reader) {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerEntityBooleanReader(namespaced(key), type, reader);
    }

    /**
     * Registers a pose driver usable from animator JSON as "modid:name" (the driver is named
     * without the mod id): see {@link KumoDriver}.
     */
    public <T extends DriverItemTemplate> void registerDriver(KumoDriver<T> driver) {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerDriver(driver.renamed(namespaced(driver.name)));
    }

    /**
     * Registers a pose driver usable from animator JSON as "modid:key", the internal way, which
     * Mo' Bends' own drivers still use: prefer {@link #registerDriver(KumoDriver)}.
     */
    public <T extends DriverItemTemplate> void registerDriver(String key, IDriverFactory<T> factory, Class<T> templateType) {
        Addons.checkRegistrationOpen();
        DriverRegistry.INSTANCE.register(namespaced(key), factory, templateType);
    }

    /**
     * Registers a component model definitions can give an entity's data, as "modid:key" (their
     * {@code components}: {@code {"swordTrail": "mobends:sword_trail"}}).
     */
    public void registerComponent(String key, EntityComponents.Factory factory) {
        Addons.checkRegistrationOpen();
        EntityComponents.register(namespaced(key), factory);
    }

    /**
     * Registers a render layer model definitions can name, as "modid:key" (their {@code layers}).
     * The renderers' layers of class {@code layerClass} (and its subclasses), drawn for a bent
     * entity whose definition names it, can then follow the bones: they ask where with
     * {@code BendsLayers.translateToBone(entity, "modid:key", bone, poseStack)}, which says no when
     * the entity isn't bent or its definition doesn't name the layer.
     */
    public void registerLayer(String key, Class<?> layerClass) {
        Addons.checkRegistrationOpen();
        DefinedLayers.register(namespaced(key), layerClass);
    }

    /**
     * Registers a render layer that draws its own copy of the entity's model (a charged creeper's
     * glow, a sheep's wool), as "modid:key": for an entity whose definition names it, the copy is
     * drawn bent, as the entity is.
     */
    public void registerCopyLayer(String key, Class<?> layerClass) {
        Addons.checkRegistrationOpen();
        DefinedLayers.registerCopy(namespaced(key), layerClass);
    }

    /**
     * Registers a render layer as "modid:key" that applies to every definition bending its entity,
     * whether the definition names it or not: for a copy of the model no definition can know about,
     * which, left unbent, would show the entity twice.
     */
    public void registerImplicitLayer(String key, Class<?> layerClass, DefinedLayers.Role role) {
        Addons.checkRegistrationOpen();
        DefinedLayers.registerImplicit(namespaced(key), layerClass, role);
    }

    private String namespaced(String key) {
        if (key.indexOf(':') >= 0) {
            throw new IllegalArgumentException("'" + key + "' is registered as '" + modId + ":" + key + "': name it without a namespace.");
        }
        return modId + ":" + key;
    }
}
