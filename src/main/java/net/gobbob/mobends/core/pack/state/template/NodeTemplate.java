package net.gobbob.mobends.core.pack.state.template;

import net.gobbob.mobends.core.pack.state.LayerType;

import java.util.List;

public class NodeTemplate
{

    public List<ConnectionTemplate> connections;
    public List<LayerTemplateEntry> layers;

    public static class LayerTemplateEntry
    {

        public LayerType type;
        public int index;

    }

}
