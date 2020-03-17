package goblinbob.mobends.core.animation.keyframe;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.*;

public class AnimationLoader
{

    public static KeyframeAnimation loadFromFile(File file) throws FileNotFoundException
    {
        JsonReader fileReader = new JsonReader(new FileReader(file));
        return (new Gson()).fromJson(fileReader, KeyframeAnimation.class);
    }

    public static KeyframeAnimation loadFromString(String animationJson)
    {
        return (new Gson()).fromJson(animationJson, KeyframeAnimation.class);
    }

    public static KeyframeAnimation loadFromResource(ResourceLocation location) throws IOException
    {
        InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
        return (new Gson()).fromJson(new InputStreamReader(stream), KeyframeAnimation.class);
    }

}
