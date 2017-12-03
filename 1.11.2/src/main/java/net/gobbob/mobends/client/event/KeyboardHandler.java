package net.gobbob.mobends.client.event;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.client.gui.GuiBendsMenu;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.PackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyboardHandler {
	public static final KeyBinding key_Menu = new KeyBinding("Mo'Bends Menu", Keyboard.KEY_G, "GobBob's Mods");
	
	@SubscribeEvent
	public void onKeyPressed(InputEvent.KeyInputEvent event) throws IOException{
		if(key_Menu.isPressed()){
			//ArmorDatabase.armorList.clear();
			Minecraft.getMinecraft().displayGuiScreen(new GuiBendsMenu());
			MoBends.refreshModel++;
			PackManager.initPacks();
		}
	}
}
