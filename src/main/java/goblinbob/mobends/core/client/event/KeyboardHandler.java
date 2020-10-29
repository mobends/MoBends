package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.pack.PackDataProvider;
import goblinbob.mobends.core.util.GsonResources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyboardHandler
{

    private static final String MAIN_CATEGORY = "Mo' Bends";
    private static final KeyBinding KEY_MENU = new KeyBinding("Mo' Bends Menu", Keyboard.KEY_G, MAIN_CATEGORY);
    private static final KeyBinding KEY_REFRESH = new KeyBinding("Refresh Animations", Keyboard.KEY_F10, MAIN_CATEGORY);

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
        }
        else if (KEY_REFRESH.isPressed())
        {
            AnimationLoader.clearCache();
            GsonResources.clearCache();
            PackDataProvider.INSTANCE.clearCache();
            EntityDatabase.instance.refresh();
            EntityBenderRegistry.instance.refreshMutators();
            Addons.onRefresh();
        }
    }

}
