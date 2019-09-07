package net.gobbob.mobends.core.flux;

import java.util.List;

public interface ISubscriber
{

    List<Subscription<?>> getSubscriptions();

    default void trackSubscription(Subscription<?> observer)
    {
        getSubscriptions().add(observer);
    }

    default void removeSubscriptions()
    {
        for (Subscription subscription : getSubscriptions())
        {
            subscription.unsubscribe();
        }
        getSubscriptions().clear();
    }

}
