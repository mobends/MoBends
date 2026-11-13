package goblinbob.mobends.lab.sim;

/**
 * A deterministic input timeline for one entity kind: the script fills the inputs for a tick.
 */
public class Scenario
{
    @FunctionalInterface
    public interface InputScript
    {
        void fill(int tick, EntityInputs inputs);
    }

    public final EntityKind kind;
    public final String name;
    public final int fps;
    public final int ticks;
    public final InputScript script;

    /** Optional one-time preparation of the entity data (e.g. forcing a zombie's animation set). */
    public final java.util.function.Consumer<goblinbob.mobends.core.data.LivingEntityData<?>> setup;

    public Scenario(EntityKind kind, String name, int fps, int ticks, InputScript script)
    {
        this(kind, name, fps, ticks, script, null);
    }

    public Scenario(EntityKind kind, String name, int fps, int ticks, InputScript script, java.util.function.Consumer<goblinbob.mobends.core.data.LivingEntityData<?>> setup)
    {
        this.kind = kind;
        this.name = name;
        this.fps = fps;
        this.ticks = ticks;
        this.script = script;
        this.setup = setup;
    }

    public String id()
    {
        return kind.id() + "/" + name;
    }

    /**
     * What the scenario asks for that 1.8.9 doesn't have (see {@link EntityInputs#laterVersionsOnly}), or null. Its
     * golden was recorded on 1.12.2, with the feature; on 1.8.9 the scenario can't be played.
     */
    public String laterVersionsOnly()
    {
        final EntityInputs inputs = new EntityInputs();
        for (int tick = 0; tick < ticks; tick++)
        {
            inputs.reset();
            script.fill(tick, inputs);
            final String unsupported = inputs.laterVersionsOnly(kind == EntityKind.PLAYER);
            if (unsupported != null) return unsupported;
        }
        return null;
    }

    public int frameCount()
    {
        return ticks * fps / 20;
    }
}
