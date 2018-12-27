package net.gobbob.mobends.client;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.event.EntityRenderHandler;
import net.gobbob.mobends.client.event.KeyboardHandler;
import net.gobbob.mobends.main.CommonProxy;
import net.gobbob.mobends.main.ModStatics;
import net.gobbob.mobends.pack.PackManager;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

public class ClientProxy extends CommonProxy
{
	public static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation(
			"textures/misc/enchanted_item_glint.png");
	public static final ResourceLocation TEXTURE_NULL = new ResourceLocation(ModStatics.MODID, "textures/white.png");

	public void preInit(Configuration config)
	{
		preInitCommon(config);
		AnimatedEntity.registerRegularRenderers();
	}

	public void init(Configuration config)
	{
		PackManager.initialize(config);
		AnimatedEntity.register(config);
		BendsVariable.init();
		KeyboardHandler.initKeyBindings();

		MinecraftForge.EVENT_BUS.register(new EntityRenderHandler());
		MinecraftForge.EVENT_BUS.register(new DataUpdateHandler());
		MinecraftForge.EVENT_BUS.register(new KeyboardHandler());
	}

	public void postInit()
	{
	}
}
