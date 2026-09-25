package goblinbob.mobends.core.types;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.definition.DefinedBenders;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.types.selector.CoreSelectorConditions;
import goblinbob.mobends.core.types.selector.ISelectorCondition;
import goblinbob.mobends.core.types.selector.SelectorConditionRegistry;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Every entity type: one built in for each addon bender, and one for each type file found in
 * {@code assets/<namespace>/bends/types/} of every mod and resource pack. Decides which type
 * animates an entity (see {@code misc/kumo-format.md}, "Entity types and selectors").
 */
public class EntityTypeRegistry
{

    public static final EntityTypeRegistry INSTANCE = new EntityTypeRegistry();

    private final Map<String, EntityType> types = new LinkedHashMap<>();
    private boolean loaded;

    /** What a type does to an entity: the bender (null: vanilla) and the factory of the entity's data. */
    public static final class Selection
    {
        public final EntityType type;
        @Nullable
        public final EntityBender<?> bender;
        @Nullable
        public final IEntityDataFactory<?> dataFactory;

        Selection(EntityType type, @Nullable EntityBender<?> bender, @Nullable IEntityDataFactory<?> dataFactory)
        {
            this.type = type;
            this.bender = bender;
            this.dataFactory = dataFactory;
        }
    }

    /**
     * The types that can apply to one entity: the stable ones whose selector held (decided once),
     * and the unstable ones, asked again every frame.
     */
    public static final class Candidates
    {
        public static final Candidates NONE = new Candidates(Collections.emptyList(), Collections.emptyList());

        private final List<EntityType> matched;
        private final List<EntityType> unstable;
        @Nullable
        private final EntityType decided;

        Candidates(List<EntityType> matched, List<EntityType> unstable)
        {
            this.matched = matched;
            this.unstable = unstable;
            this.decided = unstable.isEmpty() ? TypeOrder.first(matched) : null;
        }

        /** The selection of the type that applies to the entity, or null if it stays vanilla. */
        @Nullable
        public Selection select(EntityLivingBase entity)
        {
            EntityType winner = decided;
            if (!unstable.isEmpty())
            {
                List<EntityType> candidates = new ArrayList<>(matched);
                for (EntityType type : unstable)
                {
                    if (type.matches(entity))
                    {
                        candidates.add(type);
                    }
                }
                winner = TypeOrder.first(candidates);
            }
            if (winner == null)
            {
                return null;
            }
            Selection selection = winner.selectionFor(entity);
            return selection == null || selection.bender == null ? null : selection;
        }
    }

    /** Finds the types that can apply to the entity. */
    public Candidates candidatesFor(EntityLivingBase entity)
    {
        ensureLoaded();
        List<EntityType> matched = new ArrayList<>();
        List<EntityType> unstable = new ArrayList<>();
        for (EntityType type : types.values())
        {
            if (type.selectionFor(entity) == null)
            {
                continue;
            }
            if (!type.isStable())
            {
                unstable.add(type);
            }
            else if (type.matches(entity))
            {
                matched.add(type);
            }
        }
        return matched.isEmpty() && unstable.isEmpty() ? Candidates.NONE : new Candidates(matched, unstable);
    }

    public void ensureLoaded()
    {
        if (!loaded)
        {
            reload();
        }
    }

    /** (Re)loads every type: the built-in ones of the registered benders, then the type files. */
    public void reload()
    {
        loaded = true;
        CoreClientConfig config = CoreClient.getInstance() != null ? CoreClient.getInstance().getConfiguration() : null;
        EntityBenderRegistry benders = EntityBenderRegistry.instance;
        benders.removeTypeBenders();
        types.clear();

        for (EntityBender<?> bender : benders.getDefaultBenders())
        {
            add(EntityType.builtIn(bender));
        }

        for (TypeFileDiscovery.TypeFile file : TypeFileDiscovery.discover())
        {
            try
            {
                add(load(file, config));
            }
            catch (Exception e)
            {
                Core.LOG.log(Level.SEVERE, "Could not load the type " + file.source + ": " + e.getMessage());
            }
        }

        if (config != null)
        {
            for (EntityType type : types.values())
            {
                type.setRank(config.getTypeRank(type.getId()));
            }
        }
        benders.clearCache();
        Core.LOG.info(String.format("Loaded %d entity types", types.size()));
    }

    private void add(EntityType type)
    {
        EntityType existing = types.get(type.getId());
        if (existing != null)
        {
            Core.LOG.warning(String.format("Two types have the id '%s': using %s, ignoring %s", type.getId(), existing.getSource(), type.getSource()));
            return;
        }
        types.put(type.getId(), type);
    }

    private EntityType load(TypeFileDiscovery.TypeFile file, @Nullable CoreClientConfig config) throws Exception
    {
        EntityTypeDefinition definition = EntityTypeDefinition.parse(file.json);
        ISelectorCondition selector = definition.selector == null ? null : SelectorConditionRegistry.INSTANCE.parse(definition.selector);

        EntityBender<?> model = null;
        boolean vanilla = false;
        if (EntityTypeDefinition.VANILLA_MODEL.equals(definition.model))
        {
            vanilla = true;
        }
        else if (definition.isModelDefinition())
        {
            ResourceLocation location = new ResourceLocation(definition.model);
            model = DefinedBenders.createBender(location.getResourceDomain(), ModelDefinitions.INSTANCE.load(location));
            EntityBender<?> existing = EntityBenderRegistry.instance.getByKey(model.getKey());
            if (existing != null)
            {
                // Another type (or an addon) already made the bender with this key.
                model = existing;
            }
            else
            {
                EntityBenderRegistry.instance.registerTypeBender(model, config);
            }
        }
        else if (definition.model != null)
        {
            model = EntityBenderRegistry.instance.getByKey(definition.model);
            if (model == null)
            {
                throw new MalformedKumoTemplateException("There is no model '" + definition.model + "'.");
            }
        }

        ResourceLocation animator = definition.animator == null ? null : new ResourceLocation(definition.animator);
        return new EntityType(definition.id, file.source, selector, definition.specificity(), model, vanilla, animator, false);
    }

    public List<EntityType> getTypes()
    {
        ensureLoaded();
        return new ArrayList<>(types.values());
    }

    /**
     * The types that can animate the entities of this bender, in precedence order: its built-in
     * type, the types with it as the model, and the types without a model whose selector names an
     * entity type that has it as the default model (or names none).
     */
    public List<EntityType> getTypesFor(EntityBender<?> bender)
    {
        ensureLoaded();
        List<EntityType> found = new ArrayList<>();
        for (EntityType type : types.values())
        {
            if (type.getModel() != null)
            {
                if (type.getModel() == bender) found.add(type);
                continue;
            }
            Set<ResourceLocation> entityTypes = new HashSet<>(type.getEntityTypes());
            if (entityTypes.isEmpty())
            {
                found.add(type);
                continue;
            }
            for (ResourceLocation entityType : entityTypes)
            {
                Class<?> entityClass = CoreSelectorConditions.PLAYER.equals(entityType)
                        ? AbstractClientPlayer.class
                        : EntityList.getClass(entityType);
                if (entityClass != null && EntityBenderRegistry.instance.getDefaultBender(entityClass) == bender)
                {
                    found.add(type);
                    break;
                }
            }
        }
        found.sort(TypeOrder.PRECEDENCE);
        return found;
    }

    /** Ranks {@code order} (the first gets precedence) and stores the ranks in the config. */
    public void setOrder(List<EntityType> order)
    {
        CoreClientConfig config = CoreClient.getInstance().getConfiguration();
        for (int i = 0; i < order.size(); i++)
        {
            EntityType type = order.get(i);
            type.setRank(order.size() - 1 - i);
            config.setTypeRank(type.getId(), type.getRank());
        }
        EntityBenderRegistry.instance.clearCache();
    }

    /** Puts the types back to rank 0, so specificity and ids decide again. */
    public void resetRanks(List<EntityType> types)
    {
        CoreClientConfig config = CoreClient.getInstance().getConfiguration();
        for (EntityType type : types)
        {
            type.setRank(0);
            config.clearTypeRank(type.getId());
        }
        EntityBenderRegistry.instance.clearCache();
    }

}
