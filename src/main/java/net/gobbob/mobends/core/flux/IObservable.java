package net.gobbob.mobends.core.flux;

import java.util.Set;

public interface IObservable<T>
{

    T getValue();

    Set<IObserver<T>> getObservers();

    default Subscription<T> subscribe(IObserver<T> observer)
    {
        getObservers().add(observer);
        return new Subscription<T>(this, observer);
    }

    default void unsubscribe(IObserver<T> observer)
    {
        getObservers().remove(observer);
    }

}
