package goblinbob.mobends.core.flux;

import java.util.HashSet;
import java.util.Set;

public class Computed<T> implements IObservable<T>, IObserver
{

    /**
     * True, if the this computed property should be reevaluated.
     */
    private boolean dirty = true;
    private T value;
    private IComputedExpression<T> expression;
    private Set<IObserver<T>> observers;

    public Computed(IComputedExpression<T> expression)
    {
        this.expression = expression;
        this.observers = new HashSet<>();
        ComputedDependencyHelper.dirtyComputedSet.add(this);
    }

    @Override
    public T getValue()
    {
        ComputedDependencyHelper.linkDependency(this);

        if (dirty)
        {
            // Keeping track of the currently evaluated, so that
            // the observables inside the expression can attach themselves
            // onto the dependency graph.
            ComputedDependencyHelper.evaluatedStack.push(this);

            T newValue = expression.compute();

            for (IObserver<T> observer : observers)
            {
                observer.onChanged(newValue);
            }

            value = newValue;
            dirty = false;

            ComputedDependencyHelper.evaluatedStack.pop();
        }
        return value;
    }

    @Override
    public Set<IObserver<T>> getObservers()
    {
        return observers;
    }

    @Override
    public void onChanged(Object newValue)
    {
        dirty = true;
        ComputedDependencyHelper.dirtyComputedSet.add(this);
    }

}
