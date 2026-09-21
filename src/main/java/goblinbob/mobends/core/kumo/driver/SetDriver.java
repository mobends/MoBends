package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.pose.ValueSource;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.VariableScope;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.SetTemplate;

/**
 * Assigns a variable every frame the item is evaluated (see {@link SetTemplate}). The item's
 * {@code when} condition, handled by the node, makes it conditional.
 */
public class SetDriver implements IPoseItem
{

    private final String variable;
    private final ValueSource value;
    private final boolean nodeScope;

    public SetDriver(String variable, ValueSource value, boolean nodeScope)
    {
        this.variable = variable;
        this.value = value;
        this.nodeScope = nodeScope;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, SetTemplate template) throws MalformedKumoTemplateException
    {
        if (template.variable == null || template.value == null)
        {
            throw new MalformedKumoTemplateException("core:set needs a 'variable' and a 'value'.");
        }
        return new SetDriver(template.variable, ValueSource.fromTemplate(template.value, null), "node".equals(template.scope));
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        VariableScope scope = nodeScope ? context.getNodeScope() : context.getLayerScope();
        if (scope != null)
        {
            scope.set(variable, value.get(context));
        }
    }

    @Override
    public boolean isFinished(float elapsedTicks)
    {
        return false;
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
