package goblinbob.mobends.core.util;

import net.minecraft.resources.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/** LAB ONLY. Resolves "namespace:path" the way Minecraft does: assets/namespace/path on the classpath. */
public final class ClasspathResources {
    private ClasspathResources() {
    }

    public static InputStream open(ResourceLocation location) throws IOException {
        String path = "assets/" + location.getNamespace() + "/" + location.getPath();
        InputStream stream = ClasspathResources.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new FileNotFoundException("Resource not on the lab classpath: " + path);
        }
        return stream;
    }
}
