package goblinbob.mobends.fabric;

//? if fabric {
import goblinbob.mobends.platform.Platform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class FabricPlatform implements Platform {
    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
//?}
