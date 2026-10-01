package goblinbob.mobends.core.kumo.state.template;

import java.util.List;
import java.util.Map;

/**
 * A branch of a selector: when {@code when} holds (or it has none), it leads to {@code target} (a
 * node or machine of the selector's machine) or chooses among {@code branches}. The transition
 * fields apply when it moves the layer; null ones are taken from the enclosing branch.
 */
public class BranchTemplate
{

    public TriggerConditionTemplate when;

    /** JSON: {@code then} as a string. */
    public String target;

    /** JSON: {@code then} as a list. */
    public List<BranchTemplate> branches;

    public Float transitionDuration;

    public ConnectionTemplate.Easing transitionEasing;

    /** Layer variables assigned when the branch moves the layer (after its enclosing branches' ones). */
    public Map<String, Float> set;

}
