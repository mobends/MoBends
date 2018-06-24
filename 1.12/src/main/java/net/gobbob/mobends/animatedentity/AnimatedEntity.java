package net.gobbob.mobends.animatedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.animatedentity.previewer.PlayerPreviewer;
import net.gobbob.mobends.animatedentity.previewer.Previewer;
import net.gobbob.mobends.client.renderer.entity.RenderBendsCaveSpider;
import net.gobbob.mobends.client.renderer.entity.RenderBendsHusk;
import net.gobbob.mobends.client.renderer.entity.RenderBendsPlayer;
import net.gobbob.mobends.client.renderer.entity.RenderBendsSkeleton;
import net.gobbob.mobends.client.renderer.entity.RenderBendsSpectralArrow;
import net.gobbob.mobends.client.renderer.entity.RenderBendsSpider;
import net.gobbob.mobends.client.renderer.entity.RenderBendsStray;
import net.gobbob.mobends.client.renderer.entity.RenderBendsTippedArrow;
import net.gobbob.mobends.client.renderer.entity.RenderBendsWitherSkeleton;
import net.gobbob.mobends.client.renderer.entity.RenderBendsZombie;
import net.gobbob.mobends.util.BendsLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class AnimatedEntity
{
	public static HashMap<String, AnimatedEntity> animatedEntities = new HashMap<String, AnimatedEntity>();

	private String name;
	private String displayName;
	private List<AlterEntry> alterEntries = new ArrayList<AlterEntry>();
	private String[] alterableParts;

	public Class<? extends Entity> entityClass;
	public Render renderer;
	public Previewer previewer;

	public AnimatedEntity(String id, String displayName, Class<? extends Entity> entityClass, Render renderer,
			String[] alterableParts)
	{
		this.name = id;
		this.displayName = displayName;
		this.entityClass = entityClass;
		this.renderer = renderer;
		this.alterableParts = alterableParts;
		this.addAlterEntry(new AlterEntry(this, displayName));
	}

	public List<AlterEntry> getAlredEntries()
	{
		return this.alterEntries;
	}

	public AlterEntry getAlterEntry(int index)
	{
		return this.alterEntries.get(index);
	}

	public String[] getAlterableParts()
	{
		return alterableParts;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getName()
	{
		return name;
	}
	
	public Previewer getPreviewer()
	{
		return this.previewer;
	}

	public AnimatedEntity addAlterEntry(AlterEntry alterEntry)
	{
		this.alterEntries.add(alterEntry);
		return this;
	}

	public AnimatedEntity setPreviewer(Previewer previewer)
	{
		this.previewer = previewer;
		return this;
	}

	public static void register(Configuration config)
	{
		BendsLogger.info("Registering Animated Entities...");

		animatedEntities.clear();

		registerEntity(config,
				new AnimatedEntity("player", "Player", AbstractClientPlayer.class,
						new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager()),
						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
								"rightLeg", "leftForeLeg", "rightForeLeg", "playerRotation", "leftItemRotation",
								"rightItemRotation" })
				.setPreviewer(new PlayerPreviewer()));

		/*
		 * registerEntity(config, new AnimatedEntity("zombie", "Zombie",
		 * EntityZombie.class, new
		 * RenderBendsZombie(Minecraft.getMinecraft().getRenderManager()), new String[]
		 * {"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
		 * "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}));
		 * 
		 * registerEntity(config, new AnimatedEntity("husk", "Husk", EntityHusk.class,
		 * new RenderBendsHusk(Minecraft.getMinecraft().getRenderManager()), new
		 * String[] {"head", "body", "leftArm", "rightArm", "leftForeArm",
		 * "rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}));
		 * registerEntity(config, new AnimatedEntity("spider", "Spider",
		 * EntitySpider.class, new
		 * RenderBendsSpider(Minecraft.getMinecraft().getRenderManager()), new String[]
		 * {"head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6",
		 * "leg7", "leg8", "foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5",
		 * "foreLeg6", "foreLeg7", "foreLeg8"})); registerEntity(config, new
		 * AnimatedEntity("cave_spider", "Cave Spider", EntityCaveSpider.class, new
		 * RenderBendsCaveSpider(Minecraft.getMinecraft().getRenderManager()), new
		 * String[] {"head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5",
		 * "leg6", "leg7", "leg8", "foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4",
		 * "foreLeg5", "foreLeg6", "foreLeg7", "foreLeg8"})); registerEntity(config, new
		 * AnimatedEntity("skeleton", "Skeleton", EntitySkeleton.class, new
		 * RenderBendsSkeleton(Minecraft.getMinecraft().getRenderManager()), new
		 * String[] {"head", "body", "leftArm", "rightArm", "leftForeArm",
		 * "rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}));
		 * registerEntity(config, new AnimatedEntity("wither_skeleton",
		 * "Wither Skeleton", EntityWitherSkeleton.class, new
		 * RenderBendsWitherSkeleton(Minecraft.getMinecraft().getRenderManager()), new
		 * String[] {"head", "body", "leftArm", "rightArm", "leftForeArm",
		 * "rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}));
		 * registerEntity(config, new AnimatedEntity("stray", "Stray",
		 * EntityStray.class, new
		 * RenderBendsStray(Minecraft.getMinecraft().getRenderManager()), new String[]
		 * {"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
		 * "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}));
		 */

		RenderingRegistry.registerEntityRenderingHandler(EntitySpectralArrow.class,
				new RenderBendsSpectralArrow(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityTippedArrow.class,
				new RenderBendsTippedArrow(Minecraft.getMinecraft().getRenderManager()));
	}

	public static void registerEntity(Configuration config, AnimatedEntity argEntity)
	{
		BendsLogger.info("Registering " + argEntity.displayName);
		for (int a = 0; a < argEntity.alterEntries.size(); a++)
		{
			AlterEntry alterEntry = argEntity.alterEntries.get(a);
			alterEntry.setAnimate(config.get("Animated", alterEntry.getName(), true).getBoolean());
		}
		if (argEntity.alterEntries.get(0).isAnimated())
			RenderingRegistry.registerEntityRenderingHandler(argEntity.entityClass, argEntity.renderer);
		animatedEntities.put(argEntity.name, argEntity);
	}

	public static AnimatedEntity get(String name)
	{
		return (AnimatedEntity) animatedEntities.get(name);
	}

	public static AnimatedEntity getByEntity(Entity entity)
	{
		for (AnimatedEntity animatedEntity : animatedEntities.values())
		{
			if (animatedEntity.entityClass.isInstance(entity))
			{
				return animatedEntity;
			}
		}
		return null;
	}
}
