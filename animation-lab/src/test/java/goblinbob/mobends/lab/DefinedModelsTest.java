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
import goblinbob.mobends.core.types.EntityTypeDefinition;
import goblinbob.mobends.lab.sim.EntityInputs;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.lab.sim.LabClock;
import goblinbob.mobends.lab.sim.ScriptedEntity;
import goblinbob.mobends.lab.sim.VanillaModelInputs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every mob a shipped type file gives a model definition (bends/types/) must build a data class whose
 * bones cover what its animator drives, and the animator must produce finite, moving poses while the
 * mob walks. They have no golden traces, so this is a structural smoke test.
 */
public class DefinedModelsTest
{
    private static final Path BENDS = LabPaths.root().resolve("../src/main/resources/assets/mobends/bends");

    /** The model definitions the shipped type files name, by type id. */
    private static Map<String, ResourceLocation> definedModels() throws Exception
    {
        Map<String, ResourceLocation> models = new TreeMap<>();
        try (Stream<Path> files = Files.list(BENDS.resolve("types")))
        {
            for (Path file : files.sorted().collect(Collectors.toList()))
            {
                EntityTypeDefinition type = EntityTypeDefinition.parse(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
                if (type.isModelDefinition())
                {
                    models.put(type.id, new ResourceLocation(type.model));
                }
            }
        }
        return models;
    }

    /**
     * An entity of the definition's class, which its entity scope reads; a zombie when the class has
     * no stub here (any living entity will do then, as long as the scope reads no field).
     */
    static EntityLivingBase entityOf(EntityModelDefinition definition, World world) throws Exception
    {
        try
        {
            return (EntityLivingBase) Class.forName(definition.entity).getConstructor(World.class).newInstance(world);
        }
        catch (ClassNotFoundException e)
        {
            return new EntityZombie(world);
        }
    }

    @Test
    void everyModelDefinitionHasAType() throws Exception
    {
        // Built, but not yet any mob's: the player keeps its Java model until its type switches (task 22).
        Set<String> building = new TreeSet<>(Collections.singletonList("player.json"));
        Set<String> named = new TreeSet<>();
        for (ResourceLocation model : definedModels().values())
        {
            named.add(model.getResourcePath());
        }
        try (Stream<Path> files = Files.list(BENDS.resolve("models")))
        {
            for (Path file : files.collect(Collectors.toList()))
            {
                if (building.contains(file.getFileName().toString())) continue;
                assertTrue(named.contains("bends/models/" + file.getFileName()), file.getFileName() + " is named by no type file, so no mob uses it");
            }
        }
    }

    @TestFactory
    List<DynamicTest> definedMobsAnimate() throws Exception
    {
        return definedModels().entrySet().stream()
                .map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> check(entry.getKey(), entry.getValue())))
                .collect(Collectors.toList());
    }

    private void check(String name, ResourceLocation model) throws Exception
    {
        EntityModelDefinition definition = ModelDefinitions.INSTANCE.load(model);
        LabBootstrap.ensure();
        net.minecraft.entity.Entity.resetIds();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        EntityLivingBase entity = entityOf(definition, world);
        ScriptedEntity scripted = new ScriptedEntity(entity, world);
        DefinedEntityData<EntityLivingBase> data = DefinedEntityData.create(definition, entity);

        for (String bone : definition.allBoneNames())
        {
            assertNotNull(data.getPart(bone), name + ": bone " + bone + " has no transform");
            assertNotNull(data.getBone(bone), name + ": bone " + bone + " is not a sink");
        }

        AnimatorTemplate template = KumoSession.loadAnimator(definition.animator);
        KumoAnimatorState animator = new KumoAnimatorState(definition.entityScope(entity.getClass()), template, true,
                Collections.emptyList(), Collections.emptyList(), KumoSession.INSTANCING);
        Skeleton skeleton = animator.getSkeleton();
        for (int i = 0; i < skeleton.size(); i++)
        {
            assertNotNull(data.getBone(skeleton.nameOf(i)), name + ": the animator drives '" + skeleton.nameOf(i) + "', which the definition does not declare");
        }

        // A leg (its first segment, if it is split) must move once the mob walks: a bone named a leg (a creeper's body is split, its legs aren't), else one with a split.
        String leg = definition.bones.stream().filter(b -> b.name.toLowerCase().contains("leg")).map(b -> b.name).findFirst()
                .orElse(definition.bones.stream().filter(b -> b.split != null).map(b -> b.name).findFirst().orElse(definition.bones.get(0).name));
        LabClock clock = new LabClock(30);
        EntityInputs inputs = new EntityInputs();
        VanillaModelInputs modelInputs = new VanillaModelInputs();
        Quaternion standing = null;
        Map<String, Quaternion> stood = new HashMap<>();
        double moved = 0;
        boolean walked = false;
        // Stands for 40 ticks, walks until tick 100, then stands again until tick 180.
        for (int frame = 0; frame < 270; frame++)
        {
            int ticksStarted = clock.nextFrame();
            for (int t = 0; t < ticksStarted; t++)
            {
                int tick = clock.getTick() - ticksStarted + t + 1;
                inputs.reset();
                if (tick >= 40 && tick < 100) inputs.forwardSpeed = 0.15;
                inputs.headYaw = (float) (Math.sin(tick * 0.1) * 30);
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

            for (String bone : definition.allBoneNames())
            {
                Quaternion q = data.getPart(bone).rotation.getSmooth();
                assertTrue(Float.isFinite(q.x) && Float.isFinite(q.y) && Float.isFinite(q.z) && Float.isFinite(q.w), name + ": bone " + bone + " is not finite on frame " + frame);
            }
            Quaternion legNow = data.getPart(leg).rotation.getSmooth();
            if (frame == 30)
            {
                standing = new Quaternion(legNow.x, legNow.y, legNow.z, legNow.w);
                for (String bone : definition.allBoneNames())
                {
                    Quaternion q = data.getPart(bone).rotation.getSmooth();
                    stood.put(bone, new Quaternion(q.x, q.y, q.z, q.w));
                }
            }
            if (standing != null && frame < 150)
            {
                moved = Math.max(moved, Math.abs(legNow.x - standing.x) + Math.abs(legNow.y - standing.y) + Math.abs(legNow.z - standing.z));
            }
            if (frame == 140)
            {
                walked = animator.getCurrentNodes().contains("walk");
            }
        }
        assertTrue(moved > 0.05, name + ": the leg '" + leg + "' did not move while walking (max quaternion change " + moved + ")");
        assertTrue(walked, name + ": the animator is not in its walk node while walking");

        // Having stopped, every split segment settles back where it stood before walking (not necessarily straight: a creeper leans at rest).
        for (BoneDefinition bone : definition.bones)
        {
            if (bone.split == null) continue;
            for (String segment : bone.segmentNames())
            {
                Quaternion q = data.getPart(segment).rotation.getSmooth();
                Quaternion s = stood.get(segment);
                double dot = q.x * s.x + q.y * s.y + q.z * s.z + q.w * s.w;
                double degrees = Math.toDegrees(2 * Math.acos(Math.min(1, Math.abs(dot))));
                assertTrue(degrees < 2, String.format("%s: '%s' is still %.1f° off its standing pose after the mob stopped walking", name, segment, degrees));
            }
        }
    }
}
