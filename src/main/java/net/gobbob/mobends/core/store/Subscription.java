package net.gobbob.mobends.core.store;

public class Subscription<T>
{

    private Observable<T> observable;
    private IObserver observer;

    public Subscription(Observable<T> observable, IObserver observer) {
        this.observable = observable;
        this.observer = observer;
    }

    public void unsubscribe() {
        this.observable.unsubscribe(this.observer);
    }

}
