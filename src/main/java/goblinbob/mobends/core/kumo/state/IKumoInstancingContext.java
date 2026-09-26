package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * This is a context available during template instancing, which should provide all necessary instantiation data.
 *
 * @author Iwo Plaza
 */
public interface IKumoInstancingContext
{

    KeyframeAnimation getAnimation(String key);

    /** Resolves another animator by key, for {@code "extends"}. Null if unsupported or missing. */
    default goblinbob.mobends.core.kumo.state.template.AnimatorTemplate getAnimator(String key)
    {
        return null;
    }

    /** The named expressions visible where a template is being instanced. */
    default ExpressionScope getExpressionScope()
    {
        return ExpressionScope.ROOT;
    }

    /** This context, inside a scope that declares {@code expressions} (see {@link ExpressionScope#child}). */
    default IKumoInstancingContext withExpressions(@Nullable Map<String, goblinbob.mobends.core.kumo.expr.ExpressionTemplate> expressions) throws MalformedKumoTemplateException
    {
        ExpressionScope scope = getExpressionScope().child(expressions);
        return scope == getExpressionScope() ? this : new ScopedInstancingContext(this, scope);
    }

}
