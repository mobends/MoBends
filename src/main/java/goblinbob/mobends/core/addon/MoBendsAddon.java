package goblinbob.mobends.core.addon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an {@link IAddon} for NeoForge to find: Mo' Bends makes one with its public no-argument
 * constructor, under the id of the mod whose jar it's in. Fabric ignores it (it reads the
 * {@code "mobends"} entrypoint of {@code fabric.mod.json} instead), so a mod on both loaders can
 * have both.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MoBendsAddon {
}
