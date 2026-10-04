package goblinbob.mobends.core.kumo.api;

import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;

/**
 * A registered driver: a pose item computed in Java, {@code {"mymod:wag": {...}}}. Its fields are
 * read into {@code template} (a Gson class), and its binder, run once per use when the animator
 * loads, turns them into inputs, states, bones and declared state, and returns its evaluator.
 */
public final class KumoDriver<T extends DriverItemTemplate>
{

    public final String name;
    public final Class<T> template;
    public final DriverBinder<T> binder;

    private KumoDriver(String name, Class<T> template, DriverBinder<T> binder)
    {
        this.name = name;
        this.template = template;
        this.binder = binder;
    }

    public static <T extends DriverItemTemplate> KumoDriver<T> of(String name, Class<T> template, DriverBinder<T> binder)
    {
        return new KumoDriver<>(name, template, binder);
    }

    /** This driver, renamed (an addon's registry adds its mod id). */
    public KumoDriver<T> renamed(String name)
    {
        return new KumoDriver<>(name, template, binder);
    }

}
