package goblinbob.mobends.core.util;

import java.net.URI;

public class WebUtils
{
    public static boolean openUrlInBrowser(String url)
    {
        try
        {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke((Object)null);
            oclass.getMethod("browse", URI.class).invoke(object, new URI(url));
            return true;
        }
        catch (Throwable throwable)
        {
            return false;
        }
    }
}
