package net.gobbob.mobends.client;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.event.EventHandlerKeyboard;
import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.gobbob.mobends.configuration.SettingsManager;
import net.gobbob.mobends.main.CommonProxy;
import net.gobbob.mobends.main.ModStatics;
import net.gobbob.mobends.pack.PackManager;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy{
	public static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	public static final ResourceLocation TEXTURE_NULL = new ResourceLocation(ModStatics.MODID,"textures/white.png");
	
	public void preInit(Configuration config) {
		preInitCommon(config);
		SettingsManager.loadConfiguration(config);
	}
	
	public void init(Configuration config) {
		PackManager.initialize(config);
		AnimatedEntity.register(config);
		BendsVariable.init();
		
		ClientRegistry.registerKeyBinding(EventHandlerKeyboard.key_Menu);
		
		FMLCommonHandler.instance().bus().register(new EventHandlerRenderPlayer());
		FMLCommonHandler.instance().bus().register(new DataUpdateHandler());
		FMLCommonHandler.instance().bus().register(new EventHandlerKeyboard());
		MinecraftForge.EVENT_BUS.register(new EventHandlerRenderPlayer());
		MinecraftForge.EVENT_BUS.register(new DataUpdateHandler());
	}
	
	public void postInit() {}
}
