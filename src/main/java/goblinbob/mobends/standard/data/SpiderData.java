package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.ModStatics;
import goblinbob.mobends.standard.kumo.spider.SpiderLegs;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;

/**
 * The Java spider's data: its bones, and the {@code legs} component its leg drivers move. Its
 * crawl is its model definition's entity scope, as for the defined spider.
 */
public class SpiderData extends LivingEntityData<EntitySpider>
{

    public ModelPartTransform spiderHead;
    public ModelPartTransform spiderNeck;
    public ModelPartTransform spiderBody;
    public ModelPartTransform[] upperLimbs;
    public ModelPartTransform[] lowerLimbs;

    private static final ResourceLocation ANIMATOR = new ResourceLocation(ModStatics.MODID, "bends/animators/spider.json");

    public SpiderData(EntitySpider entity)
    {
        super(entity);
    }

    @Override
    protected ResourceLocation getDefaultAnimator()
    {
        return ANIMATOR;
    }

    @Override
    protected ResourceLocation getModelDefinition()
    {
        return new ResourceLocation(ModStatics.MODID, "bends/models/spider.json");
    }

    @Override
    public void initModelPose()
    {
        super.initModelPose();

        this.spiderBody = new ModelPartTransform();
        this.spiderNeck = new ModelPartTransform();
        this.spiderHead = new ModelPartTransform();
        this.upperLimbs = new ModelPartTransform[SpiderLegs.LIMBS];
        this.lowerLimbs = new ModelPartTransform[SpiderLegs.LIMBS];
        for (int i = 0; i < SpiderLegs.LIMBS; ++i)
        {
            boolean odd = i % 2 == 1;
            upperLimbs[i] = new ModelPartTransform();
            upperLimbs[i].position.set(odd ? SpiderLegs.HIP_X : -SpiderLegs.HIP_X, SpiderLegs.HIP_Y, SpiderLegs.hipZ(i));
            lowerLimbs[i] = new ModelPartTransform();
            lowerLimbs[i].position.set(odd ? SpiderLegs.KNEE_X : -SpiderLegs.KNEE_X, SpiderLegs.KNEE_Y, 0F);
            nameToPartMap.put("leg" + (i + 1), upperLimbs[i]);
            nameToPartMap.put("foreLeg" + (i + 1), lowerLimbs[i]);
        }
        addComponent("legs", new SpiderLegs(this));

        nameToPartMap.put("body", spiderBody);
        nameToPartMap.put("neck", spiderNeck);
        nameToPartMap.put("head", spiderHead);
        this.spiderHead.position.set(0.0F, 15.0F, -3.0F);
        this.spiderNeck.position.set(0.0F, 15.0F, 0.0F);
        this.spiderBody.position.set(0.0F, 15.0F, 9.0F);
    }

    @Override
    public void updateParts(float ticksPerFrame)
    {
        super.updateParts(ticksPerFrame);

        this.spiderBody.update(ticksPerFrame);
        this.spiderNeck.update(ticksPerFrame);
        this.spiderHead.update(ticksPerFrame);
        for (int i = 0; i < SpiderLegs.LIMBS; ++i)
        {
            upperLimbs[i].update(ticksPerFrame);
            lowerLimbs[i].update(ticksPerFrame);
        }
    }

}
