package net.gobbob.mobends.core.client.event;

import net.gobbob.mobends.core.addon.Addons;
import net.gobbob.mobends.core.bender.EntityBenderRegistry;
import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.data.EntityDatabase;
import net.gobbob.mobends.core.pack.PackDataProvider;
import net.gobbob.mobends.core.pack.PackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyboardHandler
{

    private static final String MAIN_CATEGORY = "GobBob's Mods";
    private static final KeyBinding KEY_MENU = new KeyBinding("Mo'Bends Menu", Keyboard.KEY_G, MAIN_CATEGORY);
    private static final KeyBinding KEY_REFRESH = new KeyBinding("Mo'Bends Refresh", Keyboard.KEY_F10, MAIN_CATEGORY);

    public static void initKeyBindings()
    {
        ClientRegistry.registerKeyBinding(KEY_MENU);
        ClientRegistry.registerKeyBinding(KEY_REFRESH);
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event)
    {
        if (KEY_MENU.isPressed())
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiBendsMenu());
            PackManager.INSTANCE.initLocalPacks();
        }
        else if (KEY_REFRESH.isPressed())
        {
            PackDataProvider.INSTANCE.clearCache();
            EntityDatabase.instance.refresh();
            EntityBenderRegistry.instance.refreshMutators();
            Addons.onRefresh();
        }
    }

}
