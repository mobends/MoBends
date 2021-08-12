package goblinbob.mobends.core.util;

public class BenchmarkTopic
{
    private final String name;
    private final int maxIterations;

    private long start;
    private long accumulative = 0;
    private int iterations;

    public BenchmarkTopic(String name, int iterations)
    {
        this.name = name;
        this.maxIterations = iterations;
    }

    public void start()
    {
        this.start = System.nanoTime();
    }

    public void stop()
    {
        long stop = System.nanoTime();

        accumulative += stop - start;
        iterations++;

        if (iterations >= maxIterations)
        {
            System.out.println(String.format("[Benchmark: %s] Average %fns", name, (double) accumulative / iterations));
            iterations = 0;
            accumulative = 0;
        }
    }
}
