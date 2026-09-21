package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.state.template.ValueTemplate;

/**
 * {@code "driver": "core:set"}: assigns a layer (or node) variable every frame while the item's
 * {@code when} holds, e.g. resetting a combo counter once enough ticks have passed.
 */
public class SetTemplate extends DriverItemTemplate
{

    public String variable;

    /** The value to assign; a number or a value source. */
    public ValueTemplate value;

    /** "layer" (default) or "node". */
    public String scope = "layer";

}
