package goblinbob.mobends.core.network;

/**
 * What animation content from resource packs may do, as a server decides it (resource packs are
 * untrusted: anyone can install one; the mod and other mods are trusted).
 */
public enum ResourcePackPolicy
{
    /** Everything: resource packs animate as freely as mods. Always the case in singleplayer. */
    ALLOW,
    /**
     * Resource packs' types, extensions, animators and clips work, but the offsets their animation
     * gives the model's parts and whole body are kept close to what the trusted animation gives
     * them, and their model definitions (custom geometry) are refused.
     */
    LIMITED,
    /** Resource packs' types, extensions, animators and clips are ignored (the trusted ones are used). */
    DENY;

    public static ResourcePackPolicy parse(String value)
    {
        for (ResourcePackPolicy policy : values())
        {
            if (policy.name().equalsIgnoreCase(value)) return policy;
        }
        return LIMITED;
    }
}
