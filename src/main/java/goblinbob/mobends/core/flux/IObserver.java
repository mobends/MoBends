package goblinbob.mobends.core.flux;

@FunctionalInterface
public interface IObserver<T>
{

    /**
     * This is called by an IObservable before it's value has changed.
     * Implementing this method will allow access to both the new and old value.
     * @param newValue
     */
    void onChanged(T newValue);

}
