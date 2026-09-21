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

    public Scenario(EntityKind kind, String name, int fps, int ticks, InputScript script)
    {
        this.kind = kind;
        this.name = name;
        this.fps = fps;
        this.ticks = ticks;
        this.script = script;
    }

    public String id()
    {
        return kind.id() + "/" + name;
    }

    public int frameCount()
    {
        return ticks * fps / 20;
    }
}
