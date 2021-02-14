package goblinbob.mobends.core.data;

import goblinbob.mobends.core.IModelPart;

public interface IEntityData
{
    PropertyStorage getPropertyStorage();

    IModelPart getRootPart();

    IModelPart getPartForName(String key);

    boolean isOnGround();

    boolean isSprinting();

    boolean isStillHorizontally();
}
