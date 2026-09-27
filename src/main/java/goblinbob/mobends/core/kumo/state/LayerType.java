package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeLayerTemplate;

import java.lang.reflect.Type;

public enum LayerType
{

    KEYFRAME(KeyframeLayerTemplate.class);

    private final Type templateType;

    LayerType(Type templateType)
    {
        this.templateType = templateType;
    }

    public Type getTemplateType()
    {
        return templateType;
    }

}
