package net.gobbob.mobends.core.pack.state;

import net.gobbob.mobends.core.pack.state.template.DriverLayerTemplate;
import net.gobbob.mobends.core.pack.state.template.KeyframeLayerTemplate;

import java.lang.reflect.Type;

public enum LayerType
{

    KEYFRAME(KeyframeLayerTemplate.class),
    DRIVER(DriverLayerTemplate.class);

    private Type templateType;

    LayerType(Type templateType)
    {
        this.templateType = templateType;
    }

    public Type getTemplateType()
    {
        return templateType;
    }


}
