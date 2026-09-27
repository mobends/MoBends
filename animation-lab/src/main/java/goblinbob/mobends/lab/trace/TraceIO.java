package goblinbob.mobends.lab.trace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TraceIO
{
    /** Decimal places kept in trace files; far below any parity tolerance, keeps files small. */
    private static final int SCALE = 6;

    private static final TypeAdapter<float[]> FLOAT_ARRAY = new TypeAdapter<float[]>()
    {
        @Override
        public void write(JsonWriter out, float[] value) throws IOException
        {
            if (value == null)
            {
                out.nullValue();
                return;
            }
            out.beginArray();
            for (float f : value)
            {
                out.value(BigDecimal.valueOf(f).setScale(SCALE, RoundingMode.HALF_EVEN).stripTrailingZeros());
            }
            out.endArray();
        }

        @Override
        public float[] read(JsonReader in) throws IOException
        {
            if (in.peek() == JsonToken.NULL)
            {
                in.nextNull();
                return null;
            }
            List<Float> values = new ArrayList<>();
            in.beginArray();
            while (in.hasNext())
            {
                values.add((float) in.nextDouble());
            }
            in.endArray();
            float[] result = new float[values.size()];
            for (int i = 0; i < result.length; i++) result[i] = values.get(i);
            return result;
        }
    };

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(float[].class, FLOAT_ARRAY)
            .create();

    public static void write(PoseTrace trace, Path file) throws IOException
    {
        Files.createDirectories(file.getParent());
        trace.compact();
        try (Writer writer = new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(file)), StandardCharsets.UTF_8))
        {
            GSON.toJson(trace, writer);
        }
        trace.expand();
    }

    public static PoseTrace read(Path file) throws IOException
    {
        try (Reader reader = new InputStreamReader(new GZIPInputStream(Files.newInputStream(file)), StandardCharsets.UTF_8))
        {
            PoseTrace trace = GSON.fromJson(reader, PoseTrace.class);
            trace.expand();
            return trace;
        }
    }

    public static Path fileFor(Path goldenRoot, String entityId, String scenarioName)
    {
        return goldenRoot.resolve(entityId).resolve(scenarioName + ".json.gz");
    }
}
