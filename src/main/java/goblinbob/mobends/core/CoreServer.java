package goblinbob.mobends.core;

import goblinbob.mobends.core.configuration.CoreServerConfig;
import goblinbob.mobends.core.event.EventHandlerServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nullable;

public class CoreServer extends Core<CoreServerConfig>
{

	private static CoreServer INSTANCE;

	CoreServerConfig configuration;

	CoreServer()
	{
		INSTANCE = this;
	}

	@Override
	public CoreServerConfig getConfiguration()
	{
		return configuration;
	}

	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);

		configuration = new CoreServerConfig(event.getSuggestedConfigurationFile());

		MinecraftForge.EVENT_BUS.register(new EventHandlerServer());
	}

	@Nullable
	public static CoreServer getInstance()
	{
		return INSTANCE;
	}
	
}
