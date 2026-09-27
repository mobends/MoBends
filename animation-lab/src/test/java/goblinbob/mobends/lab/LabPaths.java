package goblinbob.mobends.lab;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LabPaths
{
    public static Path root()
    {
        String prop = System.getProperty("mobends.lab.root");
        return prop != null ? Paths.get(prop) : Paths.get("").toAbsolutePath();
    }

    public static Path golden()
    {
        return root().resolve("golden");
    }
}
