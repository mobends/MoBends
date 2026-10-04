package goblinbob.mobends.core.kumo.state.template;

import java.util.Collections;
import java.util.List;

/** A scope's statement lists, its {@code @on}: run when it is created, every frame it exists, and when it is disposed. */
public class OnTemplate
{

    public static final OnTemplate NONE = new OnTemplate(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

    public final List<StatementTemplate> enter;
    public final List<StatementTemplate> update;
    public final List<StatementTemplate> exit;

    public OnTemplate(List<StatementTemplate> enter, List<StatementTemplate> update, List<StatementTemplate> exit)
    {
        this.enter = enter;
        this.update = update;
        this.exit = exit;
    }

}
