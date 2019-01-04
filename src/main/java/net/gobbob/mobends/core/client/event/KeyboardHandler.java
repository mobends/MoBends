package net.gobbob.mobends.core.client.event;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.client.model.entity.armor.ArmorModelFactory;
import net.gobbob.mobends.core.data.EntityDatabase;
import net.gobbob.mobends.core.pack.PackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyboardHandler
{
	private static final KeyBinding KEY_MENU = new KeyBinding("Mo'Bends Menu", Keyboard.KEY_G, "GobBob's Mods");
	private static final KeyBinding KEY_REFRESH = new KeyBinding("Mo'Bends Refresh", Keyboard.KEY_F10, "GobBob's Mods");

	public static void initKeyBindings()
	{
		ClientRegistry.registerKeyBinding(KEY_MENU);
		ClientRegistry.registerKeyBinding(KEY_REFRESH);
	}
	
	@SubscribeEvent
	public void onKeyPressed(InputEvent.KeyInputEvent event) throws IOException
	{
		if (KEY_MENU.isPressed())
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiBendsMenu());
			PackManager.initPacks();
		}
		else if (KEY_REFRESH.isPressed())
		{
			EntityDatabase.instance.refresh();
			AnimatedEntityRegistry.refreshMutators();
			ArmorModelFactory.refresh();
		}
	}
}
