package goblinbob.mobends.neoforge;

//? if neoforge {
/*import goblinbob.mobends.MoBends;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/^* The NeoForge entrypoint. ^/
@Mod(MoBends.MOD_ID)
public class MoBendsNeoForge {
    public MoBendsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(NeoForgePlatform::registerPayloads);
        MoBends.init();
        if (FMLEnvironment.dist.isClient()) {
            // In a class of its own, so a server never loads the client's screens.
            NeoForgeClient.registerConfigScreen(modContainer);
            //? if >=26.1 {
            /^NeoForgeClient.registerReloadListener(modEventBus);
            ^///?}
        }
    }
}
*///?}
