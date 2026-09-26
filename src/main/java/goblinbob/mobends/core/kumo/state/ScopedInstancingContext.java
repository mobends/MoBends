package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;

/** An instancing context with the named expressions of one level of the animator (see {@link ExpressionScope}). */
public class ScopedInstancingContext implements IKumoInstancingContext
{

    private final IKumoInstancingContext parent;
    private final ExpressionScope scope;

    ScopedInstancingContext(IKumoInstancingContext parent, ExpressionScope scope)
    {
        this.parent = parent instanceof ScopedInstancingContext ? ((ScopedInstancingContext) parent).parent : parent;
        this.scope = scope;
    }

    @Override
    public KeyframeAnimation getAnimation(String key)
    {
        return parent.getAnimation(key);
    }

    @Override
    public AnimatorTemplate getAnimator(String key)
    {
        return parent.getAnimator(key);
    }

    @Override
    public ExpressionScope getExpressionScope()
    {
        return scope;
    }

}
