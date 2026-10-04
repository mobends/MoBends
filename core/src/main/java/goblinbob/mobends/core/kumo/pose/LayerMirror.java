package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.MirrorTemplate;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;

import java.util.Collections;
import java.util.List;

/** Runtime form of {@link MirrorTemplate}: the condition and the slot pairing. */
public class LayerMirror
{

    private final Skeleton skeleton;
    private final ITriggerCondition when;
    private final List<List<String>> pairs;
    private int[] pairOf = new int[0];

    public LayerMirror(Skeleton skeleton, MirrorTemplate template, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        this.skeleton = skeleton;
        this.when = template.when == null ? null : TriggerConditionRegistry.INSTANCE.createFromTemplate(template.when, scope);
        this.pairs = template.pairs == null ? Collections.emptyList() : template.pairs;
        if (template.negate != null)
        {
            throw new MalformedKumoTemplateException("A mirror rule no longer negates inputs (\"negate\"): an item that follows a world direction, "
                    + "such as the head turned by headYaw, isn't mirrored, or only swaps sides (\"swapSides\").");
        }
        for (List<String> pair : this.pairs)
        {
            if (pair == null || pair.size() != 2)
            {
                throw new MalformedKumoTemplateException("A mirror pair needs exactly two bone names.");
            }
            // Register the bones so both sides of every pair have a slot.
            skeleton.indexOf(pair.get(0));
            skeleton.indexOf(pair.get(1));
        }
    }

    /** Starts the condition over, for a node of the layer being entered. */
    public void onNodeStarted(ITriggerConditionContext context)
    {
        if (when != null)
        {
            when.onNodeStarted(context);
        }
    }

    public boolean isActive(ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        return when == null || when.isConditionMet(context);
    }

    /** Slot -> its mirror slot (itself for unpaired bones); rebuilt when the skeleton grew. */
    public int[] pairing()
    {
        if (pairOf.length != skeleton.size())
        {
            pairOf = new int[skeleton.size()];
            for (int i = 0; i < pairOf.length; i++)
            {
                pairOf[i] = i;
            }
            for (List<String> pair : pairs)
            {
                int a = skeleton.indexOf(pair.get(0));
                int b = skeleton.indexOf(pair.get(1));
                pairOf[a] = b;
                pairOf[b] = a;
            }
        }
        return pairOf;
    }

}
