package goblinbob.mobends.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DataUpdateHandler
{
    private final IDataUpdateCallback callback;

    public DataUpdateHandler(IDataUpdateCallback callback)
    {
        this.callback = callback;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END || Minecraft.getInstance().player == null || Minecraft.getInstance().isPaused())
            return;

        this.callback.updateDataOnClientTick();
    }

    @FunctionalInterface
    public interface IDataUpdateCallback
    {
        void updateDataOnClientTick();
    }
}
