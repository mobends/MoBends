package goblinbob.mobends.core.util;

public class BendsPackHelper
{

    public static String constructPackName(String displayName)
    {
        String name = displayName;
        name = name.toLowerCase();
        name = name.replace('.', ' ');
        name = name.trim();
        name = name.replace(" ", "_");
        return name;
    }

}
