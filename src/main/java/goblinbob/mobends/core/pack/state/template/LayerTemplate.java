package goblinbob.mobends.core.pack.state.template;

import goblinbob.mobends.core.pack.state.LayerType;

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
