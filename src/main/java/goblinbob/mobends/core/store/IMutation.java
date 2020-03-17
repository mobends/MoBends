package goblinbob.mobends.core.store;

@FunctionalInterface
public interface IMutation<T, P>
{

    default T mutate(T state) {
        return this.mutate(state, null);
    }

    T mutate(T state, P payload);

}
