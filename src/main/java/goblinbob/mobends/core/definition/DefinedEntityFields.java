package goblinbob.mobends.core.definition;

import goblinbob.mobends.core.kumo.expr.ReflectedEntityFields;

import javax.annotation.Nullable;
import java.util.function.ToDoubleFunction;

/**
 * How a model definition's {@code field} reads the entity: a vanilla numeric field through its
 * generated accessor (renamed in production, so reflection wouldn't find it by its development
 * name), anything else by reflection.
 */
public class DefinedEntityFields extends ReflectedEntityFields
{

    @Nullable
    @Override
    protected Step step(Class<?> type, String name)
    {
        ToDoubleFunction<Object> generated = DefinedFields.vanillaNumber(type, name);
        if (generated != null)
        {
            return new Step(double.class, generated::applyAsDouble, generated);
        }
        return super.step(type, name);
    }

}
