package goblinbob.mobends.core.types.selector;

import com.google.gson.JsonObject;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** The built-in {@code core:} selector conditions. */
public final class CoreSelectorConditions
{

    /** Players have no entity registry id; this one stands for them. */
    public static final ResourceLocation PLAYER = new ResourceLocation("minecraft", "player");

    private CoreSelectorConditions()
    {
    }

    /** {@code {"type": "core:and", "conditions": [...]}}: every condition holds. */
    public static class And implements ISelectorCondition
    {
        private final List<ISelectorCondition> conditions;

        And(List<ISelectorCondition> conditions)
        {
            this.conditions = conditions;
        }

        static ISelectorCondition create(JsonObject json, SelectorConditionRegistry registry) throws MalformedKumoTemplateException
        {
            return new And(registry.parseAll(json.get("conditions")));
        }

        @Override
        public boolean test(EntityLivingBase entity)
        {
            for (ISelectorCondition condition : conditions)
            {
                if (!condition.test(entity)) return false;
            }
            return true;
        }

        @Override
        public boolean isStable()
        {
            return conditions.stream().allMatch(ISelectorCondition::isStable);
        }

        @Override
        public void collectEntityTypes(Collection<ResourceLocation> entityTypes)
        {
            conditions.forEach(condition -> condition.collectEntityTypes(entityTypes));
        }
    }

    /** {@code {"type": "core:or", "conditions": [...]}}: at least one condition holds. */
    public static class Or implements ISelectorCondition
    {
        private final List<ISelectorCondition> conditions;

        Or(List<ISelectorCondition> conditions)
        {
            this.conditions = conditions;
        }

        static ISelectorCondition create(JsonObject json, SelectorConditionRegistry registry) throws MalformedKumoTemplateException
        {
            return new Or(registry.parseAll(json.get("conditions")));
        }

        @Override
        public boolean test(EntityLivingBase entity)
        {
            for (ISelectorCondition condition : conditions)
            {
                if (condition.test(entity)) return true;
            }
            return false;
        }

        @Override
        public boolean isStable()
        {
            return conditions.stream().allMatch(ISelectorCondition::isStable);
        }

        @Override
        public void collectEntityTypes(Collection<ResourceLocation> entityTypes)
        {
            conditions.forEach(condition -> condition.collectEntityTypes(entityTypes));
        }
    }

    /** {@code {"type": "core:not", "condition": {...}}} */
    public static class Not implements ISelectorCondition
    {
        private final ISelectorCondition condition;

        Not(ISelectorCondition condition)
        {
            this.condition = condition;
        }

        static ISelectorCondition create(JsonObject json, SelectorConditionRegistry registry) throws MalformedKumoTemplateException
        {
            return new Not(registry.parse(json.get("condition")));
        }

        @Override
        public boolean test(EntityLivingBase entity)
        {
            return !condition.test(entity);
        }

        @Override
        public boolean isStable()
        {
            return condition.isStable();
        }
    }

    /**
     * {@code {"type": "core:entity_type", "entityType": "minecraft:zombie"}} (or {@code "entityTypes": [...]}):
     * the entity's registry id is one of these; {@code minecraft:player} matches players.
     */
    public static class EntityType implements ISelectorCondition
    {
        private final Set<ResourceLocation> entityTypes = new HashSet<>();

        static ISelectorCondition create(JsonObject json, SelectorConditionRegistry registry) throws MalformedKumoTemplateException
        {
            EntityType condition = new EntityType();
            for (String id : SelectorConditionRegistry.strings(json, "entityType", "entityTypes"))
            {
                condition.entityTypes.add(new ResourceLocation(id));
            }
            return condition;
        }

        @Override
        public boolean test(EntityLivingBase entity)
        {
            ResourceLocation id = entity instanceof EntityPlayer ? PLAYER : EntityList.getKey(entity);
            return id != null && entityTypes.contains(id);
        }

        @Override
        public void collectEntityTypes(Collection<ResourceLocation> entityTypes)
        {
            entityTypes.addAll(this.entityTypes);
        }
    }

    /**
     * {@code {"type": "core:player_name", "names": ["Notch"]}} (or {@code "name"}): a player whose
     * profile name is one of these, ignoring case. Display names (nicknames, team prefixes) are
     * never used.
     */
    public static class PlayerName implements ISelectorCondition
    {
        private final Set<String> names = new HashSet<>();

        static ISelectorCondition create(JsonObject json, SelectorConditionRegistry registry) throws MalformedKumoTemplateException
        {
            PlayerName condition = new PlayerName();
            for (String name : SelectorConditionRegistry.strings(json, "name", "names"))
            {
                condition.names.add(name.toLowerCase(Locale.ROOT));
            }
            return condition;
        }

        @Override
        public boolean test(EntityLivingBase entity)
        {
            return entity instanceof EntityPlayer
                    && names.contains(((EntityPlayer) entity).getGameProfile().getName().toLowerCase(Locale.ROOT));
        }
    }

    /**
     * {@code {"type": "core:player_uuid", "uuids": ["069a79f4-44e9-4726-a5be-fca90e38aaf5"]}} (or
     * {@code "uuid"}): a player with one of these UUIDs, which stay the same when the player
     * renames.
     */
    public static class PlayerUuid implements ISelectorCondition
    {
        private final Set<UUID> uuids = new HashSet<>();

        static ISelectorCondition create(JsonObject json, SelectorConditionRegistry registry) throws MalformedKumoTemplateException
        {
            PlayerUuid condition = new PlayerUuid();
            for (String uuid : SelectorConditionRegistry.strings(json, "uuid", "uuids"))
            {
                try
                {
                    condition.uuids.add(UUID.fromString(uuid));
                }
                catch (IllegalArgumentException e)
                {
                    throw new MalformedKumoTemplateException("'" + uuid + "' is not a UUID.");
                }
            }
            return condition;
        }

        @Override
        public boolean test(EntityLivingBase entity)
        {
            return entity instanceof EntityPlayer && uuids.contains(((EntityPlayer) entity).getGameProfile().getId());
        }
    }

}
