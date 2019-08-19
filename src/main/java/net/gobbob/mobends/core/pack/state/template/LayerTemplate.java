package net.gobbob.mobends.core.pack.state.template;

import net.gobbob.mobends.core.pack.state.LayerType;

/**
 * A dummy class, that tells GSON to use a custom serializer.
 */
public class LayerTemplate
{

    private LayerType type;

    public LayerType getLayerType()
    {
        return type;
    }

}
