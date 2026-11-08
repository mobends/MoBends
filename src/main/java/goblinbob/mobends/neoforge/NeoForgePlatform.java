package goblinbob.mobends.neoforge;

//? if neoforge {
/*import goblinbob.mobends.platform.Platform;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class NeoForgePlatform implements Platform {
    @Override
    public String loaderName() {
        return "neoforge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        // The loading mod list, as ModList is not there yet while mixins are being applied.
        //? if >=1.21.9 {
        /^return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
        ^///?} else
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
*///?}
