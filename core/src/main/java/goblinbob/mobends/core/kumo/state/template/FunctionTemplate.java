package goblinbob.mobends.core.kumo.state.template;

import com.google.gson.JsonElement;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A function as a scope's {@code @functions} declares it: its parameters, in order, and its body,
 * an expression that reads them as {@code arg.<name>}. A call is the body written out at the call
 * site with the arguments in place (see misc/kumo-format.md, *Functions*).
 */
public class FunctionTemplate
{

    /** What a parameter takes: a kind as operations' parameters have them, and for a choice, its choices. */
    public static final class Param
    {
        public final String kind;
        public final List<String> choices;

        public Param(String kind, List<String> choices)
        {
            this.kind = kind;
            this.choices = choices == null ? Collections.<String>emptyList() : choices;
        }
    }

    public final Map<String, Param> params;
    public final JsonElement body;

    public FunctionTemplate(LinkedHashMap<String, Param> params, JsonElement body)
    {
        this.params = Collections.unmodifiableMap(params);
        this.body = body;
    }

}
