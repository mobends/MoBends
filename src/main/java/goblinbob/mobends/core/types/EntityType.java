package goblinbob.mobends.core.types;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.types.selector.ISelectorCondition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A loaded type: while its selector holds for an entity, the entity is animated with this model
 * and animator (unless a type with precedence also holds, see {@link TypeOrder}).
 */
public class EntityType implements TypeOrder.Ranked
{

    private final String id;
    private final String source;
    @Nullable
    private final ISelectorCondition selector;
    private final int specificity;
    /** The model; null for a type that keeps its entities vanilla. */
    @Nullable
    private final EntityBender<?> model;
    private final boolean vanilla;
    @Nullable
    private final ResourceLocation animator;
    /** The options the type file declares (besides its model definition's, which come through the entity's data). */
    private final OptionSet options;
    private int rank;
    /** The animators of the extensions of this type, in the order their layers go on top, and their options. */
    private final List<ResourceLocation> extensions = new ArrayList<>();
    private final List<OptionSet> extensionOptions = new ArrayList<>();

    private final Map<EntityBender<?>, EntityTypeRegistry.Selection> selections = new HashMap<>();
    private final EntityTypeRegistry.Selection vanillaSelection = new EntityTypeRegistry.Selection(this, null, null);

    EntityType(String id, String source, @Nullable ISelectorCondition selector, int specificity, @Nullable EntityBender<?> model,
               boolean vanilla, @Nullable ResourceLocation animator, OptionSet options)
    {
        this.options = options;
        this.id = id;
        this.source = source;
        this.selector = selector;
        this.specificity = specificity;
        this.model = model;
        this.vanilla = vanilla;
        this.animator = animator;
    }

    @Override
    public String getId()
    {
        return id;
    }

    /** Where the type came from: the pack and the file. */
    public String getSource()
    {
        return source;
    }

    @Override
    public int getRank()
    {
        return rank;
    }

    public void setRank(int rank)
    {
        this.rank = rank;
    }

    @Override
    public int getSpecificity()
    {
        return specificity;
    }

    public boolean isVanilla()
    {
        return vanilla;
    }

    @Nullable
    public EntityBender<?> getModel()
    {
        return model;
    }

    @Nullable
    public ResourceLocation getAnimator()
    {
        return animator;
    }

    /** The options the type file declares. */
    public OptionSet getOptions()
    {
        return options;
    }

    public boolean matches(EntityLivingBase entity)
    {
        return selector == null || selector.test(entity);
    }

    /** False if the selector's answer for an entity can change, so it has to be asked every frame. */
    public boolean isStable()
    {
        return selector == null || selector.isStable();
    }

    /** The entity types the selector requires; empty if it doesn't name any. */
    public Collection<ResourceLocation> getEntityTypes()
    {
        List<ResourceLocation> entityTypes = new ArrayList<>();
        if (selector != null)
        {
            selector.collectEntityTypes(entityTypes);
        }
        return entityTypes;
    }

    /**
     * What this type does to the entity, or null if it can't apply to it (a model made for another
     * entity class).
     */
    @Nullable
    EntityTypeRegistry.Selection selectionFor(EntityLivingBase entity)
    {
        if (vanilla)
        {
            return vanillaSelection;
        }
        if (model == null || !model.entityClass.isInstance(entity))
        {
            return null;
        }
        return selections.computeIfAbsent(model, b -> new EntityTypeRegistry.Selection(this, b, dataFactoryFor(b)));
    }

    /** This type's extensions, in the order their layers go on (see {@link Extension#inLayerOrder}). */
    void setExtensions(List<Extension> ordered)
    {
        List<ResourceLocation> animators = new ArrayList<>();
        List<OptionSet> options = new ArrayList<>();
        for (Extension extension : ordered)
        {
            animators.add(extension.getAnimator());
            options.add(extension.getOptions());
        }
        if (!animators.equals(extensions) || !sameOptions(options))
        {
            extensions.clear();
            extensions.addAll(animators);
            extensionOptions.clear();
            extensionOptions.addAll(options);
            selections.clear();
        }
    }

    private boolean sameOptions(List<OptionSet> options)
    {
        if (options.size() != extensionOptions.size())
        {
            return false;
        }
        for (int i = 0; i < options.size(); i++)
        {
            if (options.get(i) != extensionOptions.get(i)) return false;
        }
        return true;
    }

    public List<ResourceLocation> getExtensions()
    {
        return Collections.unmodifiableList(extensions);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private IEntityDataFactory<?> dataFactoryFor(EntityBender<?> bender)
    {
        IEntityDataFactory defaultFactory = bender.getDefaultDataFactory();
        if (animator == null && extensions.isEmpty() && options.isEmpty())
        {
            // Types that keep the model's animator share the entity's data.
            return defaultFactory;
        }
        List<ResourceLocation> overlays = new ArrayList<>(extensions);
        List<OptionSet> overlayOptions = new ArrayList<>(extensionOptions);
        return (IEntityDataFactory) entity -> {
            EntityData<?> data = defaultFactory.createEntityData(entity);
            data.setAnimator(animator, overlays, options, overlayOptions);
            return data;
        };
    }

}
