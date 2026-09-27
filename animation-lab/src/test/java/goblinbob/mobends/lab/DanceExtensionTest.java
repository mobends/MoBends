package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.types.ExtensionDefinition;
import goblinbob.mobends.lab.sim.EntityInputs;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.lab.sim.LabClock;
import goblinbob.mobends.lab.sim.ScriptedEntity;
import goblinbob.mobends.lab.sim.VanillaModelInputs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The dance example pack (misc/examples/dance-extension) on the cow's and the chicken's own animators. */
public class DanceExtensionTest
{

    private static final Path PACK = LabPaths.root().resolve("../misc/examples/dance-extension/assets/mobends_dance/bends");

    @Test
    void theExtensionsTargetTheCowAndTheChicken() throws Exception
    {
        // A defined mob's built-in type is "mobends-" + its entity's registry name.
        assertEquals("mobends:cow", extension("cow").type);
        assertEquals("mobends:chicken", extension("chicken").type);
        assertEquals(extension("cow").animator, extension("chicken").animator, "one dance for both");
    }

    @Test
    void cowsDanceWhileStandingAndStopWhenTheyWalk() throws Exception
    {
        dances("cow");
    }

    @Test
    void chickensDanceWhileStandingAndStopWhenTheyWalk() throws Exception
    {
        dances("chicken");
    }

    private static ExtensionDefinition extension(String mob) throws Exception
    {
        return ExtensionDefinition.parse(new String(Files.readAllBytes(PACK.resolve("extensions/" + mob + ".json")), StandardCharsets.UTF_8));
    }

    /** Stands until tick 60 (dancing from about tick 10), then walks until tick 120. */
    private static void dances(String mob) throws Exception
    {
        EntityModelDefinition definition = ModelDefinitions.INSTANCE.load("mobends", mob);
        LabBootstrap.ensure();
        net.minecraft.entity.Entity.resetIds();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        EntityZombie entity = new EntityZombie(world);
        ScriptedEntity scripted = new ScriptedEntity(entity, world);
        DefinedEntityData<EntityZombie> data = DefinedEntityData.create(definition, entity);

        AnimatorTemplate dance;
        try (Reader reader = Files.newBufferedReader(PACK.resolve("animators/dance.json")))
        {
            dance = KumoSerializer.INSTANCE.gson.fromJson(reader, AnimatorTemplate.class);
        }
        KumoAnimatorState animator = new KumoAnimatorState(
                KumoSession.loadAnimator(definition.animator), Collections.singletonList(dance), KumoSession.INSTANCING);

        LabClock clock = new LabClock(30);
        EntityInputs inputs = new EntityInputs();
        VanillaModelInputs modelInputs = new VanillaModelInputs();
        float minX = 0, maxX = 0, minY = 0, maxY = 0;
        for (int frame = 0; frame < 180; frame++)
        {
            int ticksStarted = clock.nextFrame();
            for (int t = 0; t < ticksStarted; t++)
            {
                int tick = clock.getTick() - ticksStarted + t + 1;
                inputs.reset();
                if (tick >= 60) inputs.forwardSpeed = 0.15;
                scripted.tick(inputs);
                data.updateClient();
            }
            data.update(clock.getPartialTicks());
            modelInputs.compute(entity, clock.getPartialTicks());
            data.headYaw = MathHelper.wrapDegrees(modelInputs.headYaw);
            data.headPitch = MathHelper.wrapDegrees(modelInputs.headPitch);
            data.limbSwing = modelInputs.limbSwing;
            data.limbSwingAmount = modelInputs.limbSwingAmount;
            data.swingProgress = modelInputs.swingProgress;
            animator.update(data, DataUpdateHandler.ticksPerFrame);

            if (frame >= 30 && frame < 90)
            {
                assertTrue(animator.getActions().contains("dance"), mob + ": not dancing on frame " + frame);
                minX = Math.min(minX, data.globalOffset.getX());
                maxX = Math.max(maxX, data.globalOffset.getX());
                minY = Math.min(minY, data.globalOffset.getY());
                maxY = Math.max(maxY, data.globalOffset.getY());
            }
        }
        assertTrue(maxY > 2 && minY > -0.01, String.format("%s bounces up on the beat: y in [%.2f, %.2f]", mob, minY, maxY));
        assertTrue(maxX > 1.2 && minX < -1.2, String.format("%s sways both ways: x in [%.2f, %.2f]", mob, minX, maxX));

        assertTrue(!animator.getActions().contains("dance"), mob + ": still dancing while walking");
        assertEquals(0, data.globalOffset.getX(), 1e-3, mob + ": back in place after the dance");
        assertEquals(0, data.globalOffset.getY(), 1e-3, mob + ": back in place after the dance");
    }

}
