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
import java.util.function.Function;

/**
 * A web page the Mo' Bends menus lead to, named by the static API, a file in the mobends-resources
 * repository, so it can change without a release. It is read on every click, off the render
 * thread; if it can't be read, or doesn't name the page, the link is the fallback.
 */
public final class WebLink
{

    /** The Customize entry of the Mo' Bends menu: meant to become the web animation editor (see TODO.md). */
    public static final WebLink EDITOR = new WebLink("officialAnimationEditorUrl", api -> api.officialAnimationEditorUrl,
            "https://mobends.com/roadmap");
    /** Where to become a supporter (the Patreon page). */
    public static final WebLink SUPPORT = new WebLink("supportUrl", api -> api.supportUrl, "https://mobends.com");
    /** Where supporters log in, so their perks reach the game. */
    public static final WebLink ACCOUNT = new WebLink("accountUrl", api -> api.accountUrl, "https://mobends.com");

    private static final String STATIC_API_URL = "https://raw.githubusercontent.com/mobends/mobends-resources/master/static-api.json";
    private static final int TIMEOUT_MS = 5000;

    private final String key;
    private final Function<StaticApi, String> urlOf;
    private final String fallbackUrl;

    private WebLink(String key, Function<StaticApi, String> urlOf, String fallbackUrl)
    {
        this.key = key;
        this.urlOf = urlOf;
        this.fallbackUrl = fallbackUrl;
    }

    /** Reads the static API on a thread of its own, then opens the link in the browser. */
    public void open()
    {
        Thread thread = new Thread(() -> {
            String url = fetch();
            Minecraft.getMinecraft().addScheduledTask(() -> GuiHelper.openUrlInBrowser(url));
        }, "Mo' Bends link: " + key);
        thread.setDaemon(true);
        thread.start();
    }

    private String fetch()
    {
        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(STATIC_API_URL).openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
            {
                StaticApi api = new Gson().fromJson(reader, StaticApi.class);
                String url = api == null ? null : urlOf.apply(api);
                // Only web pages: the link comes from outside the game.
                if (url != null && url.startsWith("https://"))
                {
                    return url;
                }
                Core.LOG.warning("The static API has no usable " + key + ": " + url);
            }
        }
        catch (IOException | JsonParseException e)
        {
            Core.LOG.warning("Could not read the static API (" + e + "), opening " + fallbackUrl);
        }
        return fallbackUrl;
    }

    private static class StaticApi
    {
        String officialAnimationEditorUrl;
        String supportUrl;
        String accountUrl;
    }

}
