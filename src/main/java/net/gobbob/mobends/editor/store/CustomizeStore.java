package net.gobbob.mobends.editor.store;

import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.editor.viewport.AlterEntryRig;
import net.gobbob.mobends.core.store.IObserver;
import net.gobbob.mobends.core.store.Store;
import net.gobbob.mobends.core.store.Subscription;

public class CustomizeStore extends Store<CustomizeState>
{

    public static CustomizeStore instance = new CustomizeStore();

    private CustomizeStore()
    {
        super(new CustomizeState()
        {
            {
                currentAnimatedEntity.next(null);
                rig.next(null);
                selectedPart.next(null);
            }
        });
    }

    public static Subscription observeAnimatedEntity(IObserver<AnimatedEntity<?>> observer)
    {
        return instance.state.currentAnimatedEntity.subscribe(observer);
    }

    public static Subscription observeAlterEntryRig(IObserver<AlterEntryRig> observer)
    {
        return instance.state.rig.subscribe(observer);
    }

    public static Subscription observeSelectedPart(IObserver<AlterEntryRig.Bone> observer)
    {
        return instance.state.selectedPart.subscribe(observer);
    }

    public static AnimatedEntity<?> getCurrentAnimatedEntity()
    {
        return instance.state.currentAnimatedEntity.getValue();
    }

    public static AlterEntryRig getRig() { return instance.state.rig.getValue(); }

    public static AlterEntryRig.Bone getSelectedPart()
    {
        return instance.state.selectedPart.getValue();
    }

    public static String[] getAlterableParts()
    {
        AnimatedEntity animatedEntity = instance.state.currentAnimatedEntity.getValue();
        if (animatedEntity != null)
            return animatedEntity.getAlterableParts();
        else
            return null;
    }

    public static boolean areChangesUnapplied()
    {
        return instance.state.changesMade;
    }

}
