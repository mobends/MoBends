package goblinbob.mobends.core.types;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.client.AnimationPolicy;
import goblinbob.mobends.core.network.ResourcePackPolicy;
import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
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
    private final Map<String, Extension> extensions = new LinkedHashMap<>();
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

        private final List<EntityType> unstable;
        /** The first of the stable types whose selector held. */
        @Nullable
        private final EntityType bestStable;

        Candidates(List<EntityType> matched, List<EntityType> unstable)
        {
            this.unstable = unstable;
            this.bestStable = TypeOrder.first(matched);
        }

        /** The selection of the type that applies to the entity, or null if it stays vanilla. */
        @Nullable
        public Selection select(EntityLivingBase entity)
        {
            EntityType winner = bestStable;
            for (EntityType type : unstable)
            {
                if (type.matches(entity) && (winner == null || TypeOrder.PRECEDENCE.compare(type, winner) < 0))
                {
                    winner = type;
                }
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

    /** The types no longer match the registered benders: they load again on next use. */
    public void markStale()
    {
        loaded = false;
        EntityBenderRegistry.instance.clearCache();
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

        ResourcePackPolicy policy = AnimationPolicy.INSTANCE.current();
        for (TypeFileDiscovery.TypeFile file : TypeFileDiscovery.discover("types"))
        {
            if (!file.trusted && policy == ResourcePackPolicy.DENY)
            {
                Core.LOG.info("The server denies resource packs' animation: ignoring the type " + file.source);
                continue;
            }
            try
            {
                EntityType type = TypeFiles.loadType(file, config, policy);
                if (type != null)
                {
                    add(type);
                }
            }
            catch (Exception | StackOverflowError e)
            {
                Core.LOG.log(Level.SEVERE, "Could not load the type " + file.source, e);
            }
        }

        if (config != null)
        {
            for (EntityType type : types.values())
            {
                type.setRank(config.getTypeRank(type.getId()));
            }
        }
        int extensionCount = loadExtensions(config);
        benders.clearCache();
        Core.LOG.info(String.format("Loaded %d entity types and %d extensions", types.size(), extensionCount));
    }

    /** Loads every extension, with its rank, and gives each type its own; returns how many there are. */
    private int loadExtensions(@Nullable CoreClientConfig config)
    {
        extensions.clear();
        ResourcePackPolicy policy = AnimationPolicy.INSTANCE.current();
        for (TypeFileDiscovery.TypeFile file : TypeFileDiscovery.discover("extensions"))
        {
            if (!file.trusted && policy == ResourcePackPolicy.DENY)
            {
                Core.LOG.info("The server denies resource packs' animation: ignoring the extension " + file.source);
                continue;
            }
            try
            {
                Extension extension = TypeFiles.loadExtension(file, config);
                if (!types.containsKey(extension.getTypeId()))
                {
                    Core.LOG.warning(String.format("The extension '%s' (%s) extends the type '%s', which doesn't exist.", extension.getId(), file.source, extension.getTypeId()));
                    continue;
                }
                Extension overridden = extensions.put(extension.getId(), extension);
                if (overridden != null)
                {
                    Core.LOG.warning(String.format("Two extensions have the id '%s': using %s, ignoring %s", extension.getId(), extension.getSource(), overridden.getSource()));
                }
            }
            catch (Exception | StackOverflowError e)
            {
                Core.LOG.log(Level.SEVERE, "Could not load the extension " + file.source, e);
            }
        }
        applyExtensions();
        return extensions.size();
    }

    /** Gives every type its extensions' animators, in the order their layers go on. */
    private void applyExtensions()
    {
        for (EntityType type : types.values())
        {
            List<Extension> own = new ArrayList<>();
            for (Extension extension : extensions.values())
            {
                if (extension.getTypeId().equals(type.getId())) own.add(extension);
            }
            type.setExtensions(Extension.layerOrder(own));
        }
    }

    /** Adds a type; one found later (in a higher-priority pack) replaces one with the same id. */
    private void add(EntityType type)
    {
        EntityType overridden = types.put(type.getId(), type);
        if (overridden != null)
        {
            Core.LOG.warning(String.format("Two types have the id '%s': using %s, ignoring %s", type.getId(), type.getSource(), overridden.getSource()));
        }
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
                Class<?> entityClass = MinecraftKumoOperations.PLAYER.equals(entityType)
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

    /** Ranks the types so {@code order} holds (the first gets precedence) and stores the ranks in the config. */
    public void setOrder(List<EntityType> order)
    {
        CoreClientConfig config = CoreClient.getInstance().getConfiguration();
        for (EntityType type : TypeOrder.rankInOrder(order))
        {
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

    /** The extensions of the types that can animate this bender's entities, in precedence order (the first goes on top). */
    public List<Extension> getExtensionsFor(EntityBender<?> bender)
    {
        Set<String> typeIds = new HashSet<>();
        for (EntityType type : getTypesFor(bender))
        {
            typeIds.add(type.getId());
        }
        List<Extension> found = new ArrayList<>();
        for (Extension extension : extensions.values())
        {
            if (typeIds.contains(extension.getTypeId())) found.add(extension);
        }
        found.sort(TypeOrder.PRECEDENCE);
        return found;
    }

    /** Ranks the extensions so {@code order} holds (the first one highest, so on top) and stores the ranks. */
    public void setExtensionOrder(List<Extension> order)
    {
        CoreClientConfig config = CoreClient.getInstance().getConfiguration();
        for (Extension extension : TypeOrder.rankInOrder(order))
        {
            config.setExtensionRank(extension.getId(), extension.getRank());
        }
        applyExtensions();
        EntityBenderRegistry.instance.clearCache();
    }

    /** Puts the extensions back to rank 0, so their ids decide the order again. */
    public void resetExtensionRanks(List<Extension> order)
    {
        CoreClientConfig config = CoreClient.getInstance().getConfiguration();
        for (Extension extension : order)
        {
            extension.setRank(0);
            config.clearExtensionRank(extension.getId());
        }
        applyExtensions();
        EntityBenderRegistry.instance.clearCache();
    }

}
