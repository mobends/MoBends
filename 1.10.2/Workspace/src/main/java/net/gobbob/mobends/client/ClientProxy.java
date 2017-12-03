package net.gobbob.mobends.client;

import net.gobbob.mobends.CommonProxy;
import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.event.EventHandler_DataUpdate;
import net.gobbob.mobends.event.EventHandler_Keyboard;
import net.gobbob.mobends.event.EventHandler_RenderPlayer;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.settings.SettingsBoolean;
import net.gobbob.mobends.settings.SettingsNode;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy{
	public static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	public static final ResourceLocation texture_NULL = new ResourceLocation(MoBends.MODID,"textures/white.png");
	
	public void preinit(Configuration config) {
		BendsPack.preInit(config);
		
		((SettingsBoolean)SettingsNode.getSetting("swordTrail")).data = config.get("General", "Sword Trail", true).getBoolean();
	}
	
	public void init(Configuration config) {
		AnimatedEntity.register(config);
		
		ClientRegistry.registerKeyBinding(EventHandler_Keyboard.key_Menu);
		
		FMLCommonHandler.instance().bus().register(new EventHandler_RenderPlayer());
		FMLCommonHandler.instance().bus().register(new EventHandler_DataUpdate());
		FMLCommonHandler.instance().bus().register(new EventHandler_Keyboard());
		MinecraftForge.EVENT_BUS.register(new EventHandler_RenderPlayer());
		MinecraftForge.EVENT_BUS.register(new EventHandler_DataUpdate());
	}
	
	public void postinit() {
		
	}
}
