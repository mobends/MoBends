package goblinbob.mobends.lab.scenarios;

import goblinbob.mobends.lab.sim.EntityKind;

import java.util.EnumMap;
import java.util.Map;

/** Which KUMO animator replaces which entity's procedural controller. */
public class Animators
{
    private static final Map<EntityKind, String> BY_KIND = new EnumMap<>(EntityKind.class);

    static
    {
        BY_KIND.put(EntityKind.ZOMBIE, "mobends:bends/animators/zombie.json");
    }

    public static String forKind(EntityKind kind)
    {
        return BY_KIND.get(kind);
    }

    public static boolean has(EntityKind kind)
    {
        return BY_KIND.containsKey(kind);
    }
}
