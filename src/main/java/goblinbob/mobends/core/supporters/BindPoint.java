package goblinbob.mobends.core.supporters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.PlayerData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum BindPoint
{
    HEAD("head", data -> data.head),
    BODY("body", data -> data.body),
    LEFT_ARM("leftArm", data -> data.leftArm),
    LEFT_FOREARM("leftForearm", data -> data.leftForeArm),
    RIGHT_ARM("rightArm", data -> data.rightArm),
    RIGHT_FOREARM("rightForearm", data -> data.rightForeArm),
    LEFT_THIGH("leftThigh", data -> data.leftLeg),
    LEFT_SHIN("leftShin", data -> data.leftForeLeg),
    RIGHT_THIGH("rightThigh", data -> data.rightLeg),
    RIGHT_SHIN("rightShin", data -> data.rightForeLeg);

    private static Map<String, BindPoint> KEY_TO_VALUE = new HashMap<>();
    static
    {
        for (BindPoint p : BindPoint.values())
        {
            KEY_TO_VALUE.put(p.getKey(), p);
        }
    }

    private String key;
    private Function<PlayerData, IModelPart> partSelector;

    BindPoint(String key, Function<PlayerData, IModelPart> partSelector)
    {
        this.key = key;
        this.partSelector = partSelector;
    }

    public String getKey()
    {
        return key;
    }

    public Function<PlayerData, IModelPart> getPartSelector()
    {
        return partSelector;
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
