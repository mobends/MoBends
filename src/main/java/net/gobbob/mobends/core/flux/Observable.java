package net.gobbob.mobends.core.flux;

import java.util.HashSet;
import java.util.Set;

public class Observable<T> implements IObservable<T>
{

    private T value;
    private Set<IObserver<T>> observers;

    public Observable()
    {
        this.observers = new HashSet<>();
    }

    public Observable(T value)
    {
        this();
        this.value = value;
    }

    public void next(T newValue)
    {
        for (IObserver<T> observer : observers)
        {
            observer.onChanged(newValue);
        }
        value = newValue;
    }

    @Override
    public T getValue()
    {
        ComputedDependencyHelper.linkDependency(this);
        return value;
    }

    @Override
    public Set<IObserver<T>> getObservers()
    {
        return observers;
    }

}
