package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.state.template.ValueTemplate;

/** {@code "driver": "core:offset"}: sets a bone's position offset (model units) from value sources. */
public class OffsetTemplate extends DriverItemTemplate
{

    public String bone;
    public ValueTemplate x;
    public ValueTemplate y;
    public ValueTemplate z;

}
