package net.gobbob.mobends.core.client.event;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.gobbob.mobends.client.mutators.PlayerMutator;
import net.gobbob.mobends.core.EntityDatabase;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.client.model.entity.armor.ArmorModelFactory;
import net.gobbob.mobends.core.main.MoBends;
import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.pack.PackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyboardHandler
{
	public static final KeyBinding key_Menu = new KeyBinding("Mo'Bends Menu", Keyboard.KEY_G, "GobBob's Mods");
	public static final KeyBinding key_Refresh = new KeyBinding("Mo'Bends Refresh", Keyboard.KEY_F10, "GobBob's Mods");

	public static void initKeyBindings()
	{
		ClientRegistry.registerKeyBinding(key_Menu);
		ClientRegistry.registerKeyBinding(key_Refresh);
	}
	
	@SubscribeEvent
	public void onKeyPressed(InputEvent.KeyInputEvent event) throws IOException
	{
		if (key_Menu.isPressed())
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiBendsMenu());
			PackManager.initPacks();
		}
		else if (key_Refresh.isPressed())
		{
			EntityDatabase.instance.refresh();
			AnimatedEntity.refreshMutators();
			ArmorModelFactory.refresh();
		}
	}
}
