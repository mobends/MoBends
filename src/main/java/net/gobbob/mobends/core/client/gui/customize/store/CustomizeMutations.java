package net.gobbob.mobends.core.client.gui.customize.store;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.IPreviewer;
import net.gobbob.mobends.core.client.gui.customize.GuiCustomizeWindow;
import net.gobbob.mobends.core.client.gui.customize.IEditorAction;
import net.gobbob.mobends.core.client.gui.customize.viewport.AlterEntryRig;
import net.gobbob.mobends.core.store.IMutation;

public class CustomizeMutations
{

    interface Mutation<T> extends IMutation<CustomizeState, T> {}

    public static Mutation<AlterEntryRig.Bone> SELECT_PART = (CustomizeState state, AlterEntryRig.Bone bone) ->
    {
        state.selectedPart.next(bone);
        AlterEntryRig rig = state.rig.getValue();
        if (rig != null)
            rig.select(bone);
        return state;
    };

    public static Mutation<AlterEntry<?>> SHOW_ALTER_ENTRY = (CustomizeState state, AlterEntry<?> alterEntry) ->
    {
        if (state.currentAlterEntry.getValue() == alterEntry)
            return state;

        IPreviewer previewer = alterEntry.getPreviewer();
        if (previewer != null)
        {
            state.rig.next(new AlterEntryRig(alterEntry));
        }
        else
        {
            state.rig.next(null);
        }

        state.currentAlterEntry.next(alterEntry);

        return state;
    };

    public static Mutation<IEditorAction<GuiCustomizeWindow>> TRACK_EDITOR_ACTION = (CustomizeState state, IEditorAction<GuiCustomizeWindow> action) ->
    {
        state.actionHistory.add(action);
        return state;
    };

    public static Mutation<AlterEntryRig.Bone> HOVER_OVER_BONE = (CustomizeState state, AlterEntryRig.Bone bone) ->
    {
        AlterEntryRig rig = state.rig.getValue();
        if (rig != null)
        {
            rig.hoverOver(bone);
        }
        return state;
    };

}
