package goblinbob.mobends.core;

import goblinbob.bendslib.math.IQuaternion;
import goblinbob.bendslib.math.vector.IVec3f;

public interface IModelPart
{
    void syncUp(IModelPart part);
    void setVisible(boolean showModel);
    IVec3f getPosition();
    IVec3f getScale();
    IVec3f getOffset();
    IQuaternion getRotation();
    float getOffsetScale();
    IVec3f getGlobalOffset();
    boolean isShowing();
}
