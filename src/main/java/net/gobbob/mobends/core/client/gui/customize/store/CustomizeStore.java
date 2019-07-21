package net.gobbob.mobends.core.client.gui.customize.store;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.client.gui.customize.viewport.AlterEntryRig;
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
                currentAlterEntry.next(null);
                rig.next(null);
                selectedPart.next(null);
            }
        });
    }

    public static Subscription observeAlterEntry(IObserver<AlterEntry<?>> observer)
    {
        return instance.state.currentAlterEntry.subscribe(observer);
    }

    public static Subscription observeAlterEntryRig(IObserver<AlterEntryRig> observer)
    {
        return instance.state.rig.subscribe(observer);
    }

    public static Subscription observeSelectedPart(IObserver<AlterEntryRig.Bone> observer)
    {
        return instance.state.selectedPart.subscribe(observer);
    }

    public static AlterEntry getCurrentAlterEntry()
    {
        return instance.state.currentAlterEntry.getValue();
    }

    public static AlterEntryRig getRig() { return instance.state.rig.getValue(); }

    public static AlterEntryRig.Bone getSelectedPart()
    {
        return instance.state.selectedPart.getValue();
    }

    public static String[] getAlterableParts()
    {
        AlterEntry alterEntry = instance.state.currentAlterEntry.getValue();
        if (alterEntry != null)
            return alterEntry.getOwner().getAlterableParts();
        else
            return null;
    }

    public static boolean areChangesUnapplied()
    {
        return instance.state.changesMade;
    }

}
