package goblinbob.mobends.forge.client.event;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.error.ErrorReportRegistry;
import goblinbob.mobends.core.exceptions.InvalidPackFormatException;
import goblinbob.mobends.core.mutation.MutationInstructions;
import goblinbob.mobends.forge.AnimationLoader;
import goblinbob.mobends.forge.GsonResources;
import goblinbob.mobends.forge.ReportOutput;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class KeyboardHandler
{

    private final String MAIN_CATEGORY = "Mo' Bends";
    private final KeyBinding KEY_MENU = new KeyBinding("Mo' Bends Menu", GLFW.GLFW_KEY_G, MAIN_CATEGORY);
    private final KeyBinding KEY_REFRESH = new KeyBinding("Refresh Animations", GLFW.GLFW_KEY_F10, MAIN_CATEGORY);

    @SubscribeEvent
    public void setup(final FMLClientSetupEvent event)
    {
        ClientRegistry.registerKeyBinding(KEY_MENU);
        ClientRegistry.registerKeyBinding(KEY_REFRESH);
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event)
    {
        if (KEY_MENU.isPressed())
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
        else if (KEY_REFRESH.isPressed())
        {
            Core core = new Core();
            ReportOutput reportOutput = new ReportOutput();
            ErrorReportRegistry reportRegistry = new ErrorReportRegistry(reportOutput);

            core.registerErrors(reportRegistry);
            reportRegistry.report(new InvalidPackFormatException("Bruh", "Waddup"));

            try
            {
                KeyframeAnimation animation = AnimationLoader.loadFromPath("mobends:animations/wolf_walking.bendsanim");
                System.out.println(animation);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
