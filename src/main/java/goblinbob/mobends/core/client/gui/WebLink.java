package goblinbob.mobends.core.client.gui;

import com.google.gson.JsonParseException;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.util.ConnectionHelper;
import net.minecraft.Util;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A web page the Mo' Bends menu leads to (1.12's {@code WebLink}), named by the static API, a file in
 * the mobends-resources repository, so it can change without a release. It's read on every click,
 * in the background; if it can't be read, or doesn't name the page, the link is the fallback.
 * No screen of its own: the menu decides whether to ask before opening the browser.
 */
public final class WebLink {
    /** The Customize entry of the Mo' Bends menu: meant to become the web animation editor (see TODO.md). */
    public static final WebLink EDITOR = new WebLink("officialAnimationEditorUrl", api -> api.officialAnimationEditorUrl,
        "https://mobends.com/roadmap");
    /** Where to become a supporter (the Patreon page). */
    public static final WebLink SUPPORT = new WebLink("supportUrl", api -> api.supportUrl, "https://mobends.com");
    /** Where supporters log in, so their perks reach the game (the dashboard). */
    public static final WebLink ACCOUNT = new WebLink("accountUrl", api -> api.accountUrl, "https://mobends.com");

    private static final URI STATIC_API = URI.create("https://raw.githubusercontent.com/mobends/mobends-resources/master/static-api.json");

    private final String key;
    private final Function<StaticApi, String> urlOf;
    private final String fallbackUrl;

    private WebLink(String key, Function<StaticApi, String> urlOf, String fallbackUrl) {
        this.key = key;
        this.urlOf = urlOf;
        this.fallbackUrl = fallbackUrl;
    }

    /** The page's URL if the static API can't be read. */
    public String fallbackUrl() {
        return fallbackUrl;
    }

    /** Reads the static API in the background, then gives the page's URL (always {@code https://}) on the render thread. */
    public void fetch(Consumer<String> onUrl) {
        ConnectionHelper.runInBackground("link: " + key, () -> {
            String url = read();
            ConnectionHelper.onRenderThread(() -> onUrl.accept(url));
        });
    }

    /** Reads the static API in the background, then opens the page in the browser. */
    public void open() {
        fetch(url -> Util.getPlatform().openUri(url));
    }

    private String read() {
        try {
            String json = new String(ConnectionHelper.download(STATIC_API), StandardCharsets.UTF_8);
            StaticApi api = ConnectionHelper.GSON.fromJson(json, StaticApi.class);
            String url = api == null ? null : urlOf.apply(api);
            // Only web pages: the link comes from outside the game.
            if (url != null && url.startsWith("https://")) {
                return url;
            }
            MoBends.LOGGER.warn("The static API has no usable {}: {}", key, url);
        } catch (IOException | JsonParseException e) {
            MoBends.LOGGER.warn("Couldn't read the static API ({}), opening {}", e.toString(), fallbackUrl);
        }
        return fallbackUrl;
    }

    private static final class StaticApi {
        String officialAnimationEditorUrl;
        String supportUrl;
        String accountUrl;
    }
}
