package goblinbob.mobends.core.kumo.api;

/** Pure functions of numbers, for {@link KumoRegistry#registerFunction}. */
public final class NumberFunctions
{

    private NumberFunctions()
    {
    }

    @FunctionalInterface
    public interface Unary
    {
        float apply(float a);
    }

    @FunctionalInterface
    public interface Binary
    {
        float apply(float a, float b);
    }

    @FunctionalInterface
    public interface Ternary
    {
        float apply(float a, float b, float c);
    }

}
