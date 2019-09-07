package net.gobbob.mobends.core.flux;

import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ComputedDependencyHelper
{

    static final Stack<Computed<?>> evaluatedStack = new Stack<>();
    static final Set<Computed<?>> dirtyComputedSet = new HashSet<>();

    static void linkDependency(IObservable<?> observable)
    {
        try {
            Computed top = ComputedDependencyHelper.evaluatedStack.peek();
            observable.subscribe(top);
        }
        catch(EmptyStackException ex) {}
    }

    public static void reevaluateDirty()
    {
        for (Computed dirty : dirtyComputedSet)
        {
            dirty.getValue();
        }

        dirtyComputedSet.clear();
    }

}
