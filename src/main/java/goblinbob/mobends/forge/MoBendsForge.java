package goblinbob.mobends.forge;

//? if forge {
/*import goblinbob.mobends.MoBends;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/^* The Forge entrypoint. ^/
@Mod(MoBends.MOD_ID)
public class MoBendsForge {
    public MoBendsForge(FMLJavaModLoadingContext context) {
        MoBends.init();
        if (FMLEnvironment.dist.isClient()) {
            // In a class of its own, so a server never loads the client's screens.
            ForgeClient.registerConfigScreen(context);
        }
    }
}
*///?}
