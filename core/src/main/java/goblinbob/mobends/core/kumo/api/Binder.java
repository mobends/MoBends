package goblinbob.mobends.core.kumo.api;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;

/**
 * Binds one use of an operation, once, when the animator is loaded for an entity class: does its
 * one-time work (looks an item up, compiles a pattern) and returns how it evaluates.
 */
@FunctionalInterface
public interface Binder
{

    /**
     * @return a {@link NumberEvaluator} or a {@link BooleanEvaluator}, as the operation
     *         {@code returns}; or null if the operation doesn't apply to {@link BindArgs#entityClass()}
     *         (its {@code @fallback} is used, or the animator fails to load)
     * @throws MalformedKumoTemplateException for an argument it can't take, with
     *                                        {@link BindArgs#error}
     */
    @Nullable
    Evaluator bind(BindArgs args) throws MalformedKumoTemplateException;

}
