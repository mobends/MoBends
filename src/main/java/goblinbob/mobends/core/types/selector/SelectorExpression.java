package goblinbob.mobends.core.types.selector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.LayerState;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Map;

/**
 * A type file's selector: a boolean KUMO expression over the selector-safe operations
 * ({@code core:entity_type}, {@code core:player_name}, ...), which read the entity alone, before
 * any entity data exists.
 */
public final class SelectorExpression implements ISelectorCondition
{

    /** The operation whose arguments are the entity types a selector requires. */
    public static final String ENTITY_TYPE = "core:entity_type";

    private final Expression expression;
    private final JsonElement json;
    private final boolean stable;
    private final Context context = new Context();

    private SelectorExpression(Expression expression, JsonElement json)
    {
        this.expression = expression;
        this.json = json;
        this.stable = stable(json);
    }

    public static SelectorExpression compile(JsonElement json) throws MalformedKumoTemplateException
    {
        Expression expression = Expression.compile(json, ExpressionScope.selector(), Expression.Type.BOOLEAN);
        if (expression.isStateful())
        {
            throw new MalformedKumoTemplateException("A type file's selector remembers nothing between frames (no 'decreased', 'rose' or 'fell').");
        }
        return new SelectorExpression(expression, json);
    }

    @Override
    public boolean test(EntityLivingBase entity)
    {
        context.entity = entity;
        try
        {
            return expression.test(context);
        }
        finally
        {
            context.entity = null;
        }
    }

    @Override
    public boolean isStable()
    {
        return stable;
    }

    @Override
    public void collectEntityTypes(Collection<ResourceLocation> entityTypes)
    {
        collectEntityTypes(json, entityTypes);
    }

    /** Whether no operation in the selector can change its answer during an entity's life. */
    private static boolean stable(JsonElement json)
    {
        if (json.isJsonArray())
        {
            for (JsonElement element : json.getAsJsonArray())
            {
                if (!stable(element)) return false;
            }
        }
        else if (json.isJsonObject())
        {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
            {
                KumoOperation operation = ExpressionOperations.registered(entry.getKey());
                if (operation != null && !operation.stable) return false;
                if (!stable(entry.getValue())) return false;
            }
        }
        return true;
    }

    /** The entity types the selector requires: those {@code core:entity_type} names, except under a {@code not}. */
    private static void collectEntityTypes(JsonElement json, Collection<ResourceLocation> entityTypes)
    {
        if (json.isJsonArray())
        {
            for (JsonElement element : json.getAsJsonArray())
            {
                collectEntityTypes(element, entityTypes);
            }
        }
        else if (json.isJsonObject())
        {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
            {
                if (ENTITY_TYPE.equals(entry.getKey()) && entry.getValue().isJsonArray())
                {
                    for (JsonElement id : (JsonArray) entry.getValue())
                    {
                        entityTypes.add(new ResourceLocation(id.getAsString()));
                    }
                }
                else if (!"not".equals(entry.getKey()))
                {
                    collectEntityTypes(entry.getValue(), entityTypes);
                }
            }
        }
    }

    /**
     * How many conditions the selector has, for precedence (misc/kumo-format.md, *Precedence*):
     * an operation counts 1, but {@code and} the sum of its conditions, {@code or} the fewest of
     * any of its, {@code not} 1, and {@code if} its condition plus the fewer of its branches'.
     * A constant counts 0.
     */
    public static int countConditions(JsonElement json)
    {
        if (json == null || !json.isJsonObject())
        {
            return 0;
        }
        JsonObject object = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet())
        {
            if (entry.getKey().startsWith("@"))
            {
                continue;
            }
            JsonArray args = entry.getValue().isJsonArray() ? entry.getValue().getAsJsonArray() : new JsonArray();
            switch (entry.getKey())
            {
                case "and":
                {
                    int count = 0;
                    for (JsonElement arg : args) count += countConditions(arg);
                    return count;
                }
                case "or":
                {
                    int count = Integer.MAX_VALUE;
                    for (JsonElement arg : args) count = Math.min(count, countConditions(arg));
                    return count == Integer.MAX_VALUE ? 0 : count;
                }
                case "if":
                    return args.size() == 3 ? countConditions(args.get(0)) + Math.min(countConditions(args.get(1)), countConditions(args.get(2))) : 1;
                default:
                    return 1;
            }
        }
        return 0;
    }

    /** What the selector evaluates against: the entity, and nothing else. */
    private static final class Context implements ITriggerConditionContext, IKumoSubject
    {
        EntityLivingBase entity;

        @Override
        public IKumoSubject getSubject()
        {
            return this;
        }

        @Override
        public Object getEntity()
        {
            return entity;
        }

        @Override
        public LayerState getLayerState()
        {
            return null;
        }

        @Override
        public INodeState getCurrentNode()
        {
            return null;
        }

        @Override
        public double resolveVariable(VariableTable.Read read)
        {
            throw new IllegalStateException("A selector reads no values.");
        }

        @Override
        public long getFrame()
        {
            return 0;
        }

        @Override
        public IBoneSink getBone(String name)
        {
            return null;
        }

        @Override
        public int indexOfVariable(String name)
        {
            return -1;
        }

        @Override
        public double getVariable(int index)
        {
            return 0;
        }

        @Override
        public int indexOfState(String name)
        {
            return -1;
        }

        @Override
        public boolean getState(int index)
        {
            return false;
        }
    }

}
