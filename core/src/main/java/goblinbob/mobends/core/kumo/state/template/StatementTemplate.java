package goblinbob.mobends.core.kumo.state.template;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;

/** A statement as written in a statement list: {@code {"@when": <condition>, "set": ["layer.combo", <value>]}}. */
public class StatementTemplate
{

    /** The state it sets, a scoped name. */
    public final String target;
    public final JsonElement value;
    @Nullable
    public final JsonElement when;

    public StatementTemplate(String target, JsonElement value, @Nullable JsonElement when)
    {
        this.target = target;
        this.value = value;
        this.when = when;
    }

}
