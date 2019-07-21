package net.gobbob.mobends.core.store;

import java.util.LinkedList;
import java.util.List;

public class Observable<T>
{

    private T value;
    private List<IObserver> observers;

    public Observable()
    {
        this.observers = new LinkedList<>();
    }

    public void next(T value)
    {
        for (IObserver observer : this.observers)
        {
            observer.onChanged(value);
        }
        this.value = value;
    }

    public Subscription subscribe(IObserver<T> observer)
    {
        this.observers.add(observer);
        return new Subscription(this, observer);
    }

    public void unsubscribe(IObserver<T> observer)
    {
        this.observers.remove(observer);
    }

    public T getValue()
    {
        return this.value;
    }

}
