package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.definition.BoneDefinition;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.lab.sim.EntityInputs;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.lab.sim.LabClock;
import goblinbob.mobends.lab.sim.ReferenceSession;
import goblinbob.mobends.lab.sim.ScriptedEntity;
import goblinbob.mobends.lab.sim.VanillaModelInputs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every mob described by a model definition (bends/models/index.json) must build a data class whose
 * bones cover what its animator drives, and the animator must produce finite, moving poses while the
 * mob walks. There is no procedural reference for these mobs, so this is a structural smoke test.
 */
public class DefinedModelsTest
{
    @TestFactory
    List<DynamicTest> definedMobsAnimate() throws Exception
    {
        return ModelDefinitions.INSTANCE.index("mobends").stream()
                .map(name -> DynamicTest.dynamicTest(name, () -> check(name)))
                .collect(Collectors.toList());
    }

    private void check(String name) throws Exception
    {
        EntityModelDefinition definition = ModelDefinitions.INSTANCE.load("mobends", name);
        LabBootstrap.ensure();
        net.minecraft.entity.Entity.resetIds();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        // Any living entity will do as the subject: the definition's own entity class has no stub here.
        EntityZombie entity = new EntityZombie(world);
        ScriptedEntity scripted = new ScriptedEntity(entity, world);
        DefinedEntityData<EntityZombie> data = DefinedEntityData.create(definition, entity);

        for (String bone : definition.allBoneNames())
        {
            assertNotNull(data.getPart(bone), name + ": bone " + bone + " has no transform");
            assertNotNull(data.getBone(bone), name + ": bone " + bone + " is not a sink");
        }

        AnimatorTemplate template = KumoSession.loadAnimator(definition.animator);
        KumoAnimatorState<DefinedEntityData<EntityZombie>> animator = new KumoAnimatorState<>(template, KumoSession.INSTANCING);
        Skeleton skeleton = animator.getSkeleton();
        for (int i = 0; i < skeleton.size(); i++)
        {
            assertNotNull(data.getBone(skeleton.nameOf(i)), name + ": the animator drives '" + skeleton.nameOf(i) + "', which the definition does not declare");
        }

        // A first leg segment (a bone with a split) must move once the mob walks.
        String leg = definition.bones.stream().filter(b -> b.split != null).map(b -> b.name).findFirst().orElse(definition.bones.get(0).name);
        LabClock clock = new LabClock(30);
        EntityInputs inputs = new EntityInputs();
        VanillaModelInputs modelInputs = new VanillaModelInputs();
        Quaternion standing = null;
        double moved = 0;
        for (int frame = 0; frame < 150; frame++)
        {
            int ticksStarted = clock.nextFrame();
            for (int t = 0; t < ticksStarted; t++)
            {
                int tick = clock.getTick() - ticksStarted + t + 1;
                ReferenceSession.resetInputs(inputs);
                if (tick >= 40) inputs.forwardSpeed = 0.15;
                inputs.headYaw = (float) (Math.sin(tick * 0.1) * 30);
                scripted.tick(inputs);
                data.updateClient();
            }
            data.update(clock.getPartialTicks());
            modelInputs.compute(entity, clock.getPartialTicks());
            data.headYaw.set(MathHelper.wrapDegrees(modelInputs.headYaw));
            data.headPitch.set(MathHelper.wrapDegrees(modelInputs.headPitch));
            data.limbSwing.set(modelInputs.limbSwing);
            data.limbSwingAmount.set(modelInputs.limbSwingAmount);
            data.swingProgress.set(modelInputs.swingProgress);
            animator.update(data, DataUpdateHandler.ticksPerFrame);

            for (String bone : definition.allBoneNames())
            {
                Quaternion q = data.getPart(bone).rotation.getSmooth();
                assertTrue(Float.isFinite(q.x) && Float.isFinite(q.y) && Float.isFinite(q.z) && Float.isFinite(q.w), name + ": bone " + bone + " is not finite on frame " + frame);
            }
            Quaternion legNow = data.getPart(leg).rotation.getSmooth();
            if (frame == 30)
            {
                standing = new Quaternion(legNow.x, legNow.y, legNow.z, legNow.w);
            }
            if (standing != null)
            {
                moved = Math.max(moved, Math.abs(legNow.x - standing.x) + Math.abs(legNow.y - standing.y) + Math.abs(legNow.z - standing.z));
            }
        }
        assertTrue(moved > 0.05, name + ": the leg '" + leg + "' did not move while walking (max quaternion change " + moved + ")");
        assertTrue(animator.getActions().contains("walk"), name + ": the animator is not in its walk node while walking");
    }
}
