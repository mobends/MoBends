package net.gobbob.mobends.editor.store;

import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.IPreviewer;
import net.gobbob.mobends.editor.gui.GuiCustomizeWindow;
import net.gobbob.mobends.editor.IEditorAction;
import net.gobbob.mobends.editor.viewport.AlterEntryRig;
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

    public static Mutation<AnimatedEntity<?>> SHOW_ANIMATED_ENTITY = (CustomizeState state, AnimatedEntity<?> animatedEntity) ->
    {
        if (state.currentAnimatedEntity.getValue() == animatedEntity)
            return state;

        IPreviewer previewer = animatedEntity.getPreviewer();
        if (previewer != null)
        {
            state.rig.next(new AlterEntryRig(animatedEntity));
        }
        else
        {
            state.rig.next(null);
        }

        state.currentAnimatedEntity.next(animatedEntity);

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
