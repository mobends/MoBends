package net.gobbob.mobends.core.flux;

@FunctionalInterface
public interface IComputedExpression<T>
{

    T compute();

}
