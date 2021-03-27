package goblinbob.mobends.forge.client.event;

import goblinbob.mobends.core.mutation.MutationInstructions;
import goblinbob.mobends.forge.GsonResources;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class KeyboardHandler
{

    private final String MAIN_CATEGORY = "mobends.key.categories.main";
    private final KeyBinding KEY_MENU = new KeyBinding("mobends.key.menu", GLFW.GLFW_KEY_G, MAIN_CATEGORY);
    private final KeyBinding KEY_REFRESH = new KeyBinding("mobends.key.refresh", GLFW.GLFW_KEY_F10, MAIN_CATEGORY);

    private final RefreshAction refreshAction;

    public KeyboardHandler(RefreshAction refreshAction)
    {
        this.refreshAction = refreshAction;
    }

    public void setup()
    {
        ClientRegistry.registerKeyBinding(KEY_MENU);
        ClientRegistry.registerKeyBinding(KEY_REFRESH);
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event)
    {
        if (KEY_MENU.isDown())
        {
            MutationInstructions instructions = null;
            try
            {
                instructions = GsonResources.get(new ResourceLocation(ModStatics.MODID, "mutators/wolf.json"), MutationInstructions.class);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            System.out.println(instructions);
        }
        else if (KEY_REFRESH.isDown())
        {
            this.refreshAction.onRefresh();
        }
    }

    @FunctionalInterface
    public interface RefreshAction
    {
        void onRefresh();
    }

}
