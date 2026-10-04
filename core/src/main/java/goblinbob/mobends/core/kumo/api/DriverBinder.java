package goblinbob.mobends.core.kumo.api;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/** Binds one use of a driver, once, when the animator loads: see {@link DriverBindArgs}. */
@FunctionalInterface
public interface DriverBinder<T>
{

    DriverEvaluator bind(T template, DriverBindArgs args) throws MalformedKumoTemplateException;

}
