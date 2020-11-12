package goblinbob.mobends.forge.client.event;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.error.ErrorReportRegistry;
import goblinbob.mobends.core.exceptions.InvalidPackFormatException;
import goblinbob.mobends.forge.ReportOutput;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyboardHandler
{

    private static final String MAIN_CATEGORY = "Mo' Bends";
    private static final KeyBinding KEY_MENU = new KeyBinding("Mo' Bends Menu", GLFW.GLFW_KEY_G, MAIN_CATEGORY);
    private static final KeyBinding KEY_REFRESH = new KeyBinding("Refresh Animations", GLFW.GLFW_KEY_F10, MAIN_CATEGORY);

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
        }
        else if (KEY_REFRESH.isPressed())
        {
            Core core = new Core();
            ReportOutput reportOutput = new ReportOutput();
            ErrorReportRegistry reportRegistry = new ErrorReportRegistry(reportOutput);

            core.registerErrors(reportRegistry);
            reportRegistry.report(new InvalidPackFormatException("Bruh", "Waddup"));
        }
    }

}
