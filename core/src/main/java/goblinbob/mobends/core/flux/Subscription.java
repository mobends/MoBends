package goblinbob.mobends.core.flux;

public class Subscription<T>
{

    private IObservable<T> observable;
    private IObserver observer;

    public Subscription(IObservable<T> observable, IObserver observer) {
        this.observable = observable;
        this.observer = observer;
    }

    public void unsubscribe() {
        this.observable.unsubscribe(this.observer);
    }

}
