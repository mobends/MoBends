package goblinbob.mobends.core.kumo.state.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Locale;

/**
 * Reads every enum the format names as lower_snake_case ({@code "pre"}, {@code "ease_in_out"},
 * {@code "x"}): the format's words are lower case, as operation choices and registry ids are.
 */
public final class LowerCaseEnums implements TypeAdapterFactory
{

    public static final LowerCaseEnums INSTANCE = new LowerCaseEnums();

    private LowerCaseEnums()
    {
    }

    /** The constant of {@code type} written {@code name}, or an error naming {@code what}. */
    public static <E extends Enum<E>> E valueOf(Class<E> type, String name, String what)
    {
        if (!name.equals(name.toLowerCase(Locale.ROOT)))
        {
            throw new JsonParseException(String.format("%s: '%s' is written in lower case, '%s'.", what, name, name.toLowerCase(Locale.ROOT)));
        }
        try
        {
            return Enum.valueOf(type, name.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException e)
        {
            StringBuilder known = new StringBuilder();
            for (E constant : type.getEnumConstants())
            {
                known.append(known.length() == 0 ? "" : ", ").append(constant.name().toLowerCase(Locale.ROOT));
            }
            throw new JsonParseException(String.format("%s: unknown value '%s' (it is one of %s).", what, name, known));
        }
    }

    /** The format's spelling of {@code value}. */
    public static String nameOf(Enum<?> value)
    {
        return value.name().toLowerCase(Locale.ROOT);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> token)
    {
        Class<? super T> raw = token.getRawType();
        if (!Enum.class.isAssignableFrom(raw) || raw == Enum.class)
        {
            return null;
        }
        if (!raw.isEnum())
        {
            raw = raw.getSuperclass();
        }
        Class<? extends Enum> type = (Class<? extends Enum>) raw;
        return (TypeAdapter<T>) new TypeAdapter<Enum>()
        {
            @Override
            public void write(JsonWriter out, Enum value) throws IOException
            {
                if (value == null) out.nullValue();
                else out.value(nameOf(value));
            }

            @Override
            public Enum read(JsonReader in) throws IOException
            {
                if (in.peek() == JsonToken.NULL)
                {
                    in.nextNull();
                    return null;
                }
                return valueOf(type, in.nextString(), "A " + type.getSimpleName());
            }
        };
    }

}
