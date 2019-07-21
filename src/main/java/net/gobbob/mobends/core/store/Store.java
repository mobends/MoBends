package net.gobbob.mobends.core.store;

public class Store<T>
{

    protected T state;

    public Store(T initialState) {
        this.state = initialState;
    }

    public void commit(IMutation<T, ?> mutation) {
        this.state = mutation.mutate(this.state);
    }

    public <P> void commit(IMutation<T, P> mutation, P payload) {
        this.state = mutation.mutate(this.state, payload);
    }

}
