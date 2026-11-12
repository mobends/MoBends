package goblinbob.mobends.core.compat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * 1.8.9 posts some events on FML's bus (ticks, input, config changes, connections) and the rest on Forge's, where
 * later versions post all of them on one. A handler registered here gets both; each bus only calls the handlers of
 * the events posted on it.
 */
public final class Events
{

    private Events()
    {
    }

    public static void register(Object handler)
    {
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
    }

    public static void unregister(Object handler)
    {
        MinecraftForge.EVENT_BUS.unregister(handler);
        FMLCommonHandler.instance().bus().unregister(handler);
    }
}
