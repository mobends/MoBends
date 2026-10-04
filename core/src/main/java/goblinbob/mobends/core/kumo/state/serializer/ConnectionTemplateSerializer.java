package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.ConnectionTemplate;

import java.lang.reflect.Type;
import java.util.Map;

/** A connection: {@code {"when": <condition>, "then": "<node or machine>", ...}}, like a selector branch. */
public class ConnectionTemplateSerializer implements JsonDeserializer<ConnectionTemplate>
{

    private static final Type SET_TYPE = new TypeToken<Map<String, Float>>() {}.getType();
    private static final Map<String, String> KEYS = JsonReading.same("when", "then", "transitionDuration", "transitionEasing", "set");

    @Override
    public ConnectionTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = JsonReading.fields(JsonReading.object(json, "A connection"), "A connection", KEYS);
        ConnectionTemplate connection = new ConnectionTemplate();
        if (!object.has("then"))
        {
            throw new JsonParseException("A connection has no \"then\": the node or machine it leads to.");
        }
        connection.target = JsonReading.string(object.get("then"), "A connection's \"then\" (a node or machine name)");
        if (!object.has("when") || object.get("when").isJsonNull())
        {
            throw new JsonParseException(String.format("The connection to \"%s\" has no \"when\".", connection.target));
        }
        connection.when = new ExpressionTemplate(object.get("when"));
        if (object.has("transitionDuration"))
        {
            connection.transitionDuration = JsonReading.number(object.get("transitionDuration"), "A connection's \"transitionDuration\"");
        }
        if (object.has("transitionEasing"))
        {
            connection.transitionEasing = JsonReading.enumValue(ConnectionTemplate.Easing.class, object.get("transitionEasing"), "A connection's \"transitionEasing\"");
        }
        if (object.has("set"))
        {
            connection.set = context.deserialize(object.get("set"), SET_TYPE);
        }
        return connection;
    }

}
