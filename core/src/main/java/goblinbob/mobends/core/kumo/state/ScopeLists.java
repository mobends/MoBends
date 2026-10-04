package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.OnTemplate;

import javax.annotation.Nullable;

/**
 * A scope's definitions and statement lists, together: created when the scope is entered, updated
 * every frame it exists, disposed when it is left (see misc/kumo-format.md, *Definitions and
 * statements*).
 */
public final class ScopeLists
{

    public final DefinitionScope scope;
    private final StatementList enter;
    private final StatementList update;
    private final StatementList exit;

    private ScopeLists(DefinitionScope scope, StatementList enter, StatementList update, StatementList exit)
    {
        this.scope = scope;
        this.enter = enter;
        this.update = update;
        this.exit = exit;
    }

    /** Compiles the lists of {@code on} (null: none) in {@code place}, inside {@code scope}. */
    public static ScopeLists compile(DefinitionScope scope, @Nullable OnTemplate on, ExpressionScope place) throws MalformedKumoTemplateException
    {
        if (on == null)
        {
            on = OnTemplate.NONE;
        }
        return new ScopeLists(scope,
                StatementList.compile(on.enter, place),
                StatementList.compile(on.update, place),
                StatementList.compile(on.exit, place));
    }

    /** The scope is created: its definitions take their values, then its {@code enter} list runs. */
    public void enter(ITriggerConditionContext context)
    {
        scope.start(context);
        runEnter(context);
    }

    /**
     * Only the {@code enter} list (what it remembers started over), for a scope several files'
     * lists share, started once (an animator and the animators it extends).
     */
    public void runEnter(ITriggerConditionContext context)
    {
        enter.restart(context);
        update.restart(context);
        exit.restart(context);
        enter.run(context);
    }

    /** A frame the scope exists: its live definitions that remember step, then its {@code update} list runs. */
    public void update(ITriggerConditionContext context)
    {
        scope.updateLive(context);
        runUpdate(context);
    }

    /** Only the {@code update} list (see {@link #runEnter}). */
    public void runUpdate(ITriggerConditionContext context)
    {
        update.run(context);
    }

    /** The scope is disposed: its {@code exit} list runs. */
    public void exit(ITriggerConditionContext context)
    {
        exit.run(context);
    }

}
