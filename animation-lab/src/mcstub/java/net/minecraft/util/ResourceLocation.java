package net.minecraft.util;

public class ResourceLocation
{
    private final String domain;
    private final String path;

    public ResourceLocation(String domain, String path)
    {
        this.domain = domain;
        this.path = path;
    }

    public ResourceLocation(String full)
    {
        int idx = full.indexOf(':');
        if (idx == -1)
        {
            domain = "minecraft";
            path = full;
        }
        else
        {
            domain = full.substring(0, idx);
            path = full.substring(idx + 1);
        }
    }

    public String getResourceDomain() { return domain; }
    public String getResourcePath() { return path; }

    @Override
    public String toString() { return domain + ":" + path; }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof ResourceLocation && ((ResourceLocation) o).domain.equals(domain) && ((ResourceLocation) o).path.equals(path);
    }

    @Override
    public int hashCode() { return 31 * domain.hashCode() + path.hashCode(); }
}
