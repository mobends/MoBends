package goblinbob.mobends.core.client.gui;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.util.GuiHelper;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Where the Customize entry of the Mo' Bends menu leads: the {@code officialAnimationEditorUrl} of
 * the static API, a file in the mobends-resources repository, so it can change without a release
 * (it is meant to become the web animation editor, see TODO.md). It is read on every click, off
 * the render thread; if it can't be read, the link is the roadmap.
 */
final class EditorLink
{

    private static final String STATIC_API_URL = "https://raw.githubusercontent.com/mobends/mobends-resources/master/static-api.json";
    private static final String FALLBACK_URL = "https://mobends.com/roadmap";
    private static final int TIMEOUT_MS = 5000;

    private EditorLink()
    {
    }

    /** Reads the static API on a thread of its own, then opens the link in the browser. */
    static void open()
    {
        Thread thread = new Thread(() -> {
            String url = fetch();
            Minecraft.getMinecraft().addScheduledTask(() -> GuiHelper.openUrlInBrowser(url));
        }, "Mo' Bends editor link");
        thread.setDaemon(true);
        thread.start();
    }

    private static String fetch()
    {
        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(STATIC_API_URL).openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
            {
                StaticApi api = new Gson().fromJson(reader, StaticApi.class);
                String url = api == null ? null : api.officialAnimationEditorUrl;
                // Only web pages: the link comes from outside the game.
                if (url != null && url.startsWith("https://"))
                {
                    return url;
                }
                Core.LOG.warning("The static API has no usable officialAnimationEditorUrl: " + url);
            }
        }
        catch (IOException | JsonParseException e)
        {
            Core.LOG.warning("Could not read the static API (" + e + "), opening " + FALLBACK_URL);
        }
        return FALLBACK_URL;
    }

    private static class StaticApi
    {
        String officialAnimationEditorUrl;
    }

}
