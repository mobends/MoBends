package net.minecraft.resources;

import net.minecraft.ResourceLocationException;

import javax.annotation.Nullable;

/** Stand-in for the game's ResourceLocation: {@code namespace:path}, checked as the game checks it. */
public final class ResourceLocation {
    private static final String DEFAULT_NAMESPACE = "minecraft";

    private final String namespace;
    private final String path;

    private ResourceLocation(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public static ResourceLocation fromNamespaceAndPath(String namespace, String path) {
        if (!isValidNamespace(namespace)) {
            throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + namespace + ":" + path);
        }
        if (!isValidPath(path)) {
            throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + namespace + ":" + path);
        }
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation withDefaultNamespace(String path) {
        return fromNamespaceAndPath(DEFAULT_NAMESPACE, path);
    }

    /** {@code namespace:path}, or {@code path} in the default namespace. */
    public static ResourceLocation parse(String location) {
        int colon = location.indexOf(':');
        if (colon < 0) {
            return withDefaultNamespace(location);
        }
        String namespace = location.substring(0, colon);
        return fromNamespaceAndPath(namespace.isEmpty() ? DEFAULT_NAMESPACE : namespace, location.substring(colon + 1));
    }

    /** {@link #parse}, or null if it isn't a valid location. */
    @Nullable
    public static ResourceLocation tryParse(String location) {
        try {
            return parse(location);
        } catch (ResourceLocationException e) {
            return null;
        }
    }

    private static boolean isValidNamespace(String namespace) {
        for (int i = 0; i < namespace.length(); i++) {
            char c = namespace.charAt(i);
            if (!(c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.')) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidPath(String path) {
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (!(c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '/' || c == '.')) {
                return false;
            }
        }
        return true;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ResourceLocation other && other.namespace.equals(namespace) && other.path.equals(path);
    }

    @Override
    public int hashCode() {
        return 31 * namespace.hashCode() + path.hashCode();
    }
}
