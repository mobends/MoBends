package goblinbob.mobends.core.types;

import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.client.definition.DefinedEntities;
import goblinbob.mobends.core.configuration.ClientConfig;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.types.selector.ISelectorCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A loaded type: while its selector holds for an entity, the entity is animated with this model
 * and animator (unless a type with precedence also holds, see {@link TypeOrder}).
 */
public class EntityType implements TypeOrder.Ranked {
    private final String id;
    private final String source;
    @Nullable
    private final ISelectorCondition selector;
    private final int specificity;
    /** The model, or null for the entity's default model (or for vanilla). */
    @Nullable
    private final DefinedEntity model;
    private final boolean vanilla;
    @Nullable
    private final ResourceLocation animator;
    /** The options the type file declares (a built-in type has its model definition's, through the entity's data). */
    private final OptionSet options;
    /** The animators of the extensions of this type, in the order their layers go on top, and their options. */
    private final List<ResourceLocation> extensions = new ArrayList<>();
    private final List<OptionSet> extensionOptions = new ArrayList<>();

    private final Map<DefinedEntity, EntityTypeRegistry.Selection> selections = new HashMap<>();
    private final EntityTypeRegistry.Selection vanillaSelection = new EntityTypeRegistry.Selection(this, null, null);

    EntityType(String id, String source, @Nullable ISelectorCondition selector, int specificity, @Nullable DefinedEntity model, boolean vanilla,
               @Nullable ResourceLocation animator, OptionSet options) {
        this.id = id;
        this.source = source;
        this.selector = selector;
        this.specificity = specificity;
        this.model = model;
        this.vanilla = vanilla;
        this.animator = animator;
        this.options = options;
    }

    /** The type every entity registered in code gets: its model for the entities it's the default model of. */
    static EntityType builtIn(DefinedEntity defined, String key) {
        ISelectorCondition isDefaultModel = entity -> DefinedEntities.defaultFor(entity.getClass()) == defined;
        return new EntityType(key, "built in", isDefaultModel, 1, defined, false, null, OptionSet.EMPTY);
    }

    @Override
    public String getId() {
        return id;
    }

    /** Where the type came from: the pack and the file, or "built in". */
    public String getSource() {
        return source;
    }

    /** The order the user gave it (the Mo' Bends menu), above the others' with a higher rank. */
    @Override
    public int getRank() {
        return ClientConfig.INSTANCE.typeRank(id);
    }

    @Override
    public int getSpecificity() {
        return specificity;
    }

    public boolean matches(LivingEntity entity) {
        return selector == null || selector.test(entity);
    }

    /** False if the selector's answer for an entity can change, so it has to be asked every frame. */
    public boolean isStable() {
        return selector == null || selector.isStable();
    }

    /**
     * What this type does to the entity, or null if it can't apply to it (no default model for it,
     * or a model made for another entity class).
     */
    @Nullable
    EntityTypeRegistry.Selection selectionFor(LivingEntity entity) {
        if (vanilla) {
            return vanillaSelection;
        }
        DefinedEntity defined = model != null ? model : DefinedEntities.defaultFor(entity.getClass());
        if (defined == null || !defined.animates(entity)) {
            return null;
        }
        return selections.computeIfAbsent(defined, d -> new EntityTypeRegistry.Selection(this, d, dataFactoryFor(d)));
    }

    /** This type's extensions, in the order their layers go on (see {@link Extension#inLayerOrder}). */
    void setExtensions(List<Extension> ordered) {
        extensions.clear();
        extensionOptions.clear();
        for (Extension extension : ordered) {
            extensions.add(extension.getAnimator());
            extensionOptions.add(extension.getOptions());
        }
        selections.clear();
    }

    private IEntityDataFactory<LivingEntity> dataFactoryFor(DefinedEntity defined) {
        IEntityDataFactory<LivingEntity> defaultFactory = defined.dataFactory();
        if (animator == null && extensions.isEmpty() && options.isEmpty()) {
            // Types that keep the model's animator share the entity's data.
            return defaultFactory;
        }
        List<ResourceLocation> overlays = List.copyOf(extensions);
        List<OptionSet> overlayOptions = List.copyOf(extensionOptions);
        return entity -> {
            LivingEntityData<LivingEntity> data = defaultFactory.createEntityData(entity);
            data.setAnimator(animator, overlays, options, overlayOptions);
            return data;
        };
    }
}
