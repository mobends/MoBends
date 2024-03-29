package goblinbob.mobends.core.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ColorAdapter extends TypeAdapter<Color>
{
    @Override
    public Color read(JsonReader in) throws IOException
    {
        int colorHex = in.nextInt();
        return Color.fromHexRGB(colorHex);
    }

    @Override
    public void write(JsonWriter out, Color value) throws IOException
    {
        out.value(Color.asHex(value));
    }
}
