package goblinbob.mobends.core.supporters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.EntityData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public enum BindPoint
{
    HEAD("head", "head"),
    BODY("body", "body"),
    LEFT_ARM("leftArm", "leftArm"),
    LEFT_FOREARM("leftForearm", "leftForeArm"),
    RIGHT_ARM("rightArm", "rightArm"),
    RIGHT_FOREARM("rightForearm", "rightForeArm"),
    LEFT_THIGH("leftThigh", "leftLeg"),
    LEFT_SHIN("leftShin", "leftForeLeg"),
    RIGHT_THIGH("rightThigh", "rightLeg"),
    RIGHT_SHIN("rightShin", "rightForeLeg");

    private static final Map<String, BindPoint> KEY_TO_VALUE = new HashMap<>();
    static
    {
        for (BindPoint p : BindPoint.values())
        {
            KEY_TO_VALUE.put(p.getKey(), p);
        }
    }

    private final String key;
    /** The bone the accessory is bound to. */
    private final String bone;

    BindPoint(String key, String bone)
    {
        this.key = key;
        this.bone = bone;
    }

    public String getKey()
    {
        return key;
    }

    /** The part of {@code data} the accessory is bound to, or null if it has none. */
    public IModelPart partOf(EntityData<?> data)
    {
        Object part = data.getPartForName(bone);
        return part instanceof IModelPart ? (IModelPart) part : null;
    }

    public static BindPoint fromKey(String key)
    {
        return KEY_TO_VALUE.get(key);
    }

    public static class Adapter extends TypeAdapter<BindPoint>
    {
        @Override
        public BindPoint read(JsonReader in) throws IOException
        {
            String key = in.nextString();
            return BindPoint.fromKey(key);
        }

        @Override
        public void write(JsonWriter out, BindPoint value) throws IOException
        {
            out.value(value.getKey());
        }
    }
}
