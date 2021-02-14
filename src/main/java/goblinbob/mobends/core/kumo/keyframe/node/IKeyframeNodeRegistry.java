package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.data.IEntityData;

public interface IKeyframeNodeRegistry<D extends IEntityData>
{
    <T extends KeyframeNodeTemplate> void register(String key, IKeyframeNodeFactory<D, ?, T> factory, Class<T> templateType);
}
