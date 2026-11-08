package goblinbob.mobends.fabric;

//? if fabric {
import goblinbob.mobends.MoBends;
import net.fabricmc.api.ModInitializer;

/** The Fabric entrypoint (fabric.mod.json). */
public class MoBendsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MoBends.init();
    }
}
//?}
