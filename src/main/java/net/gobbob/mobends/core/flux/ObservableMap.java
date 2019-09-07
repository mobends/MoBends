package net.gobbob.mobends.core.flux;

import java.util.*;

public class ObservableMap<K, V> implements IObservable<Map<K, V>>
{

    private Map<K, V> map;
    private Set<IObserver<Map<K, V>>> observers;

    public ObservableMap()
    {
        this.map = new HashMap<>();
        this.observers = new HashSet<>();
    }

    public void put(K key, V value)
    {
        this.map.put(key, value);
        this.notifyObservers();
    }

    public V get(K key)
    {
        ComputedDependencyHelper.linkDependency(this);
        return this.map.get(key);
    }

    public Collection<V> values()
    {
        ComputedDependencyHelper.linkDependency(this);
        return this.map.values();
    }

    public void clear()
    {
        this.map.clear();
        this.notifyObservers();
    }

    private void notifyObservers()
    {
        for (IObserver<Map<K, V>> observer : observers)
        {
            observer.onChanged(map);
        }
    }

    @Override
    public Map<K, V> getValue()
    {
        ComputedDependencyHelper.linkDependency(this);
        return map;
    }

    @Override
    public Set<IObserver<Map<K, V>>> getObservers()
    {
        return observers;
    }

}