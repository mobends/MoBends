package goblinbob.mobends.core.kumo.expr;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * An expression as written in an animator, kept as JSON until it's compiled against the scope it
 * appears in (see {@link Expression#compile}):
 * <ul>
 *     <li>a number: a constant;</li>
 *     <li>a string: a name, the innermost named expression with that name, or else a variable;</li>
 *     <li>an object with one key: an operation, its value the list of arguments, e.g.
 *     {@code {"add": [{"mul": ["limbSwing", 0.6662]}, 3.14]}}.</li>
 * </ul>
 */
public class ExpressionTemplate
{

    public final JsonElement json;

    public ExpressionTemplate(JsonElement json)
    {
        this.json = json;
    }

    public static class Deserializer implements JsonDeserializer<ExpressionTemplate>
    {
        @Override
        public ExpressionTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return json == null || json.isJsonNull() ? null : new ExpressionTemplate(json);
        }
    }

}
