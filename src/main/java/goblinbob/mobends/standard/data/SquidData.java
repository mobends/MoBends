package goblinbob.mobends.standard.data;


import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.ModStatics;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.ResourceLocation;

public class SquidData extends LivingEntityData<EntitySquid>
{
	public static final int TENTACLE_SECTIONS = 9;
	public static final int SECTION_HEIGHT = 18 / TENTACLE_SECTIONS;

	public ModelPartTransform squidBody;
	public ModelPartTransform[][] squidTentacles;

	private static final ResourceLocation ANIMATOR = new ResourceLocation(ModStatics.MODID, "bends/animators/squid.json");
	
	public SquidData(EntitySquid entity)
	{
		super(entity);
	}
	
	@Override
	protected ResourceLocation getDefaultAnimator()
	{
		return ANIMATOR;
	}

	/** Its tentacle phase is the model definition's entity scope, as for the defined squid. */
	@Override
	protected ResourceLocation getModelDefinition()
	{
		return new ResourceLocation(ModStatics.MODID, "bends/models/squid.json");
	}

	@Override
	public void initModelPose()
	{
		super.initModelPose();

		this.squidBody = new ModelPartTransform();
		this.squidBody.rotation.finish();
		this.squidBody.position.set(0.0F, 8.0F, 0.0F);
		nameToPartMap.put("body", this.squidBody);

		this.squidTentacles = new ModelPartTransform[8][TENTACLE_SECTIONS];
		for (int i = 0; i < this.squidTentacles.length; ++i)
		{
			double d0 = (double) i * Math.PI * 2.0D / (double) this.squidTentacles.length;
			float x = (float) Math.cos(d0) * 4.0F;
			float z = (float) Math.sin(d0) * 4.0F;

			this.squidTentacles[i][0] = new ModelPartTransform();
			this.squidTentacles[i][0].position.set(x, 16.0F, z);
			nameToPartMap.put("tentacle_" + i + "_0", this.squidTentacles[i][0]);

			for (int j = 1; j < SquidData.TENTACLE_SECTIONS; ++j)
			{
				this.squidTentacles[i][j] = new ModelPartTransform();
				this.squidTentacles[i][j].rotation.finish();
				this.squidTentacles[i][j].position.set(0.0F, SECTION_HEIGHT, 0.0F);
				nameToPartMap.put("tentacle_" + i + "_" + j, this.squidTentacles[i][j]);
			}
			
			this.squidTentacles[i][1].position.set(0, SquidData.SECTION_HEIGHT, 2);
		}
	}

	@Override
	public void updateParts(float ticksPerFrame)
	{
		super.updateParts(ticksPerFrame);

		this.squidBody.update(ticksPerFrame);

		for (int i = 0; i < this.squidTentacles.length; ++i)
			for (int j = 0; j < SquidData.TENTACLE_SECTIONS; ++j)
				this.squidTentacles[i][j].update(ticksPerFrame);
	}
}
