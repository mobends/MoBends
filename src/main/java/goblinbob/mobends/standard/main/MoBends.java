package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.addon.AddonHelper;
import goblinbob.mobends.standard.DefaultAddon;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = ModStatics.MODID,
    name = ModStatics.MODNAME,
    version = ModStatics.VERSION,
    updateJSON = "https://mobends.github.io/mod-update.json"
)
public class MoBends
{

    @SidedProxy(serverSide = "net.gobbob.mobends.standard.main.CommonProxy",
                clientSide = "net.gobbob.mobends.standard.client.ClientProxy")
    public static CommonProxy proxy;

    @Instance(value = ModStatics.MODID)
    public static MoBends instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.createCore();
        Core.getInstance().preInit(event);
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        Core.getInstance().init(event);
        proxy.init();

        // Registering the standard set of animations.
        AddonHelper.registerAddon(ModStatics.MODID, new DefaultAddon());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        Core.getInstance().postInit(event);
        proxy.postInit();
    }

}
