package goblinbob.mobends;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** LAB SHIM. The mod's entry point; the animation code only needs its logger and the version it runs on. */
public final class MoBends {
    public static final String MOD_ID = "mobends";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    /** The Minecraft version the stand-ins in src/mcstub are (what core:minecraft_version compares with). */
    public static final String MINECRAFT = "1.21.1";

    private MoBends() {
    }
}
