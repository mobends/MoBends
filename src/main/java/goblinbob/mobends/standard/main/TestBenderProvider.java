package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityBenderProvider;
import goblinbob.mobends.core.IRefreshable;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.kumo.ConnectionTemplate;
import goblinbob.mobends.core.kumo.LayerTemplate;
import goblinbob.mobends.core.kumo.driver.DriverLayerTemplate;
import goblinbob.mobends.core.kumo.driver.expression.NumberFunctionCall;
import goblinbob.mobends.core.kumo.driver.expression.NumberLiteral;
import goblinbob.mobends.core.kumo.driver.instruction.SetPositionInstructionTemplate;
import goblinbob.mobends.core.kumo.driver.node.DriverNodeTemplate;
import goblinbob.mobends.core.kumo.driver.node.LookAroundDriverNodeTemplate;
import goblinbob.mobends.core.kumo.driver.node.StandardDriverNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.MovementKeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.StandardKeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.trigger.AnimationFinishedCondition;
import goblinbob.mobends.core.kumo.trigger.NotCondition;
import goblinbob.mobends.core.kumo.trigger.StateCondition;
import goblinbob.mobends.core.mutation.MutationInstructions;
import goblinbob.mobends.forge.*;
import goblinbob.mobends.standard.main.trigger.WolfStateCondition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class TestBenderProvider implements IEntityBenderProvider<ForgeMutationContext, BenderResources>, IRefreshable
{
    private final DataRepository dataRepository;
    private final PuppeteerRepository puppeteerRepository;

    public EntityBender<ForgeMutationContext, BenderResources> wolfBender;

    public TestBenderProvider()
    {
        this.dataRepository = new DataRepository();
        this.puppeteerRepository = new PuppeteerRepository(dataRepository);
    }

    public void init() throws IOException
    {
        MutationInstructions instructions = GsonResources.get(new ResourceLocation(ModStatics.MODID, "mutators/wolf.json"), MutationInstructions.class);
//        AnimatorTemplate animatorTemplate = AnimatorTemplate.deserialize(serialContext, BinaryResources.getStream(new ResourceLocation(ModStatics.MODID, "animators/wolf.bender")));
        AnimatorTemplate animatorTemplate = new AnimatorTemplate();
        animatorTemplate.layers = new LayerTemplate[] {
                new KeyframeLayerTemplate("core:keyframe", 0, null, new KeyframeNodeTemplate[] {
                        new StandardKeyframeNodeTemplate(
                                "core:standard",
                                Arrays.asList(
                                        new ConnectionTemplate(
                                                1,
                                                4,
                                                ConnectionTemplate.Easing.EASE_IN_OUT,
                                                new StateCondition.Template("core:state", StateCondition.State.MOVING_HORIZONTALLY)),
                                        new ConnectionTemplate(
                                                2,
                                                3,
                                                ConnectionTemplate.Easing.EASE_IN_OUT,
                                                new WolfStateCondition.Template("mobends:wolf_state", WolfStateCondition.State.SITTING))
                                ),
                                "mobends:animations/wolf_idle.bendsanim",
                                0,
                                1,
                                true),
                        new MovementKeyframeNodeTemplate(
                                "core:movement",
                                Arrays.asList(
                                        new ConnectionTemplate(
                                                0,
                                                4,
                                                ConnectionTemplate.Easing.EASE_IN_OUT,
                                                new StateCondition.Template("core:state", StateCondition.State.STANDING_STILL)),
                                        new ConnectionTemplate(
                                                2,
                                                1,
                                                ConnectionTemplate.Easing.EASE_IN_OUT,
                                                new WolfStateCondition.Template("mobends:wolf_state", WolfStateCondition.State.SITTING))
                                ),
                                "mobends:animations/wolf_walking.bendsanim",
                                0,
                                4),
                        new StandardKeyframeNodeTemplate( // Sitting Down
                                "core:standard",
                                Arrays.asList(
                                        new ConnectionTemplate(
                                                3,
                                                1,
                                                ConnectionTemplate.Easing.EASE_IN_OUT,
                                                new AnimationFinishedCondition.Template("core:animation_finished"))
                                ),
                                "mobends:animations/wolf_sitting_down.bendsanim",
                                6,
                                2,
                                false),
                        new StandardKeyframeNodeTemplate( // Sitting
                                "core:standard",
                                Arrays.asList(
                                        new ConnectionTemplate(
                                                4,
                                                1,
                                                ConnectionTemplate.Easing.EASE_IN_OUT,
                                                new NotCondition.Template(
                                                        "core:not",
                                                        new WolfStateCondition.Template("mobends:wolf_state", WolfStateCondition.State.SITTING)
                                                ))
                                        ),
                                "mobends:animations/wolf_sitting.bendsanim",
                                0,
                                1,
                                true),
                        new StandardKeyframeNodeTemplate( // Standing Up
                                "core:standard",
                                Arrays.asList(
                                        new ConnectionTemplate(
                                                0,
                                                1,
                                                ConnectionTemplate.Easing.EASE_IN_OUT,
                                                new AnimationFinishedCondition.Template(
                                                        "core:animation_finished"
                                                ))
                                ),
                                "mobends:animations/wolf_standing_up.bendsanim",
                                4,
                                2,
                                false),
                }),
                new DriverLayerTemplate("core:driver", 0, new DriverNodeTemplate[] {
                        new LookAroundDriverNodeTemplate( // Look Around
                                "core:look_around",
                                Collections.emptyList(),
                                "head"
                        )
                }),
                new DriverLayerTemplate("core:driver", 0, new DriverNodeTemplate[] {
                        new StandardDriverNodeTemplate( // Testing
                                "core:standard",
                                Collections.emptyList(),
                                Arrays.asList(new SetPositionInstructionTemplate(
                                        "head",
                                        new NumberLiteral<EntityData>(0.0F),
                                        new NumberFunctionCall.Template("sin", Arrays.asList(
                                                new NumberFunctionCall.Template("lifetime", Collections.emptyList())
                                        )),
                                        new NumberLiteral<EntityData>(0.0F)
                                ))
                        )
                })
        };

//        this.wolfBender = new EntityBender<>(
//                this.puppeteerRepository,
//                "mobends:wolf",
//                "wolf",
//                WolfEntity.class,
//                instructions,
//                animatorTemplate
//        );
    }

    public void updateDataOnClientTick()
    {
        this.dataRepository.updateDataOnClientTick();
    }

    @Override
    public void refresh()
    {
        this.puppeteerRepository.clear();
        try
        {
            this.init();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public EntityBender<ForgeMutationContext, BenderResources> getBenderForEntity(LivingEntity entity)
    {
        if (entity instanceof WolfEntity)
        {
            return wolfBender;
        }

        return null;
    }
}
