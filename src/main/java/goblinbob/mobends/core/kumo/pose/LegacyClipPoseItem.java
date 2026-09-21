package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * Playback rules of the original {@code core:standard} / {@code core:movement} nodes, kept so
 * existing animators (wolf, bends packs) behave as before: progress is counted in keyframes,
 * advances by {@code playbackSpeed} per tick from {@code startFrame}, wraps over
 * {@code keyframes - 1} when looping and clamps at the last keyframe otherwise. Movement clips
 * take their progress from {@code limbSwing * 0.6662 * playbackSpeed} instead.
 */
public class LegacyClipPoseItem implements IPoseItem
{

    private final ClipBinding clip;
    private final int startFrame;
    private final float playbackSpeed;
    private final boolean looping;
    private final boolean movement;

    /**
     * Movement progress, as the original node kept it: recomputed from limbSwing after the pose
     * was written, so a frame's pose uses the previous frame's limb swing.
     */
    private float movementProgress;

    public LegacyClipPoseItem(ClipBinding clip, int startFrame, float playbackSpeed, boolean looping, boolean movement)
    {
        this.clip = clip;
        this.startFrame = startFrame;
        this.playbackSpeed = playbackSpeed;
        this.looping = looping;
        this.movement = movement;
    }

    public float progressAt(IKumoContext context, float elapsedTicks)
    {
        final int last = clip.keyframeCount - 1;
        if (last <= 0)
        {
            return 0;
        }

        if (movement)
        {
            return movementProgress;
        }

        float progress = startFrame + playbackSpeed * elapsedTicks;
        if (looping)
        {
            while (progress >= last)
            {
                progress -= last;
            }
        }
        else if (progress > last)
        {
            progress = last;
        }
        return progress;
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        clip.apply(pose, progressAt(context, elapsedTicks), 1F, Pose.Space.OVERRIDE);
    }

    @Override
    public boolean isFinished(float elapsedTicks)
    {
        if (movement || looping)
        {
            return false;
        }
        return startFrame + playbackSpeed * elapsedTicks >= clip.keyframeCount - 1;
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        movementProgress = startFrame;
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
        if (movement)
        {
            final int last = clip.keyframeCount - 1;
            if (last <= 0)
            {
                movementProgress = 0;
                return;
            }
            float limbSwing = (float) context.getSubject().getVariable("limbSwing") * 0.6662F;
            movementProgress = (playbackSpeed * limbSwing) % last;
        }
    }

}
