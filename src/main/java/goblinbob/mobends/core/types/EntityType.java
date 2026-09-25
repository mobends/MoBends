package goblinbob.mobends.core.types;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.types.selector.ISelectorCondition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
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
    /** The model, or null for the entity's default model (or for vanilla). */
    @Nullable
    private final EntityBender<?> model;
    private final boolean vanilla;
    @Nullable
    private final ResourceLocation animator;
    private final boolean builtIn;
    private int rank;

    private final Map<EntityBender<?>, EntityTypeRegistry.Selection> selections = new HashMap<>();
    private final EntityTypeRegistry.Selection vanillaSelection = new EntityTypeRegistry.Selection(this, null, null);

    EntityType(String id, String source, @Nullable ISelectorCondition selector, int specificity,
               @Nullable EntityBender<?> model, boolean vanilla, @Nullable ResourceLocation animator, boolean builtIn)
    {
        this.id = id;
        this.source = source;
        this.selector = selector;
        this.specificity = specificity;
        this.model = model;
        this.vanilla = vanilla;
        this.animator = animator;
        this.builtIn = builtIn;
    }

    /** The type every addon bender gets: its model for the entities it's the default model of. */
    static EntityType builtIn(EntityBender<?> bender)
    {
        ISelectorCondition isDefaultModel = entity -> EntityBenderRegistry.instance.getDefaultBender(entity.getClass()) == bender;
        return new EntityType(bender.getKey(), "built in", isDefaultModel, 1, bender, false, null, true);
    }

    @Override
    public String getId()
    {
        return id;
    }

    /** Where the type came from: the pack and the file, or "built in". */
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

    public boolean isBuiltIn()
    {
        return builtIn;
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
     * What this type does to the entity, or null if it can't apply to it (no default model for it,
     * or a model made for another entity class).
     */
    @Nullable
    EntityTypeRegistry.Selection selectionFor(EntityLivingBase entity)
    {
        if (vanilla)
        {
            return vanillaSelection;
        }
        EntityBender<?> bender = model != null ? model : EntityBenderRegistry.instance.getDefaultBender(entity.getClass());
        if (bender == null || !bender.entityClass.isInstance(entity))
        {
            return null;
        }
        return selections.computeIfAbsent(bender, b -> new EntityTypeRegistry.Selection(this, b, dataFactoryFor(b)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private IEntityDataFactory<?> dataFactoryFor(EntityBender<?> bender)
    {
        IEntityDataFactory defaultFactory = bender.getDefaultDataFactory();
        if (animator == null)
        {
            // Types that keep the model's animator share the entity's data.
            return defaultFactory;
        }
        return (IEntityDataFactory) entity -> {
            EntityData<?> data = defaultFactory.createEntityData(entity);
            data.setAnimator(animator);
            return data;
        };
    }

}
