package goblinbob.mobends.core.kumo.state.template;

import com.google.gson.JsonElement;

/**
 * A definition as written in a scope's {@code @define}: an object with one key, its kind, whose
 * value is the expression ({@code {"live": {"mul": ["entityLimbSwing", 2]}}}).
 */
public class DefinitionTemplate
{

    public enum Kind
    {
        /** Computed once, when its scope is created. */
        CONSTANT("constant"),
        /** An initial value computed when its scope is created, changed by {@code set} statements and drivers. */
        STATE("state"),
        /** Computed once per frame. */
        LIVE("live");

        public final String key;

        Kind(String key)
        {
            this.key = key;
        }
    }

    public final Kind kind;
    public final JsonElement expression;

    public DefinitionTemplate(Kind kind, JsonElement expression)
    {
        this.kind = kind;
        this.expression = expression;
    }

}
