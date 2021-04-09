package goblinbob.mobends.forge.client.event;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyboardHandler
{
    private final String MAIN_CATEGORY = "mobends.key.categories.main";
    private final KeyBinding KEY_MENU = new KeyBinding("mobends.key.menu", GLFW.GLFW_KEY_G, MAIN_CATEGORY);

    public KeyboardHandler()
    {
    }

    public void setup()
    {
        ClientRegistry.registerKeyBinding(KEY_MENU);
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event)
    {
        if (KEY_MENU.isDown())
        {
            // TODO Open menu
        }
    }
}
