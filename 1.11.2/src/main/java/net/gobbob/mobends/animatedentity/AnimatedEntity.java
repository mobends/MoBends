package net.gobbob.mobends.animatedentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.animation.Animation;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class AnimatedEntity {
	public static HashMap<String, AnimatedEntity> animatedEntities = new HashMap<String, AnimatedEntity>();
	
	public static Map skinMap = Maps.newHashMap();
    public static RenderBendsPlayer playerRenderer;
	
	private String name;
	private String displayName;
	
	public Class<? extends Entity> entityClass;
	public Render renderer;
	
	public HashMap animations = new HashMap<String, Animation>();
	private List<AlterEntry> alterEntries = new ArrayList<AlterEntry>();
	private String[] alterableParts;
	private List<String> alterableAnimations;
	
	public AnimatedEntity(String argID, String argDisplayName, Class<? extends Entity> argClass, Render argRenderer, String[] alterableParts){
		this.name = argID;
		this.displayName = argDisplayName;
		this.entityClass = argClass;
		this.renderer = argRenderer;
		this.alterableParts = alterableParts;
		this.addAlterEntry(new AlterEntry(this, this.getDisplayName()));
		this.alterableAnimations = new ArrayList<String>();
	}

	public AnimatedEntity add(Animation animation){
		this.animations.put(animation.getName(), animation);
		this.alterableAnimations.addAll(Arrays.asList(animation.getAlterableList()));
		return this;
	}
	
	public AnimatedEntity addAlterEntry(AlterEntry alterEntry) {
		this.alterEntries.add(alterEntry);
		return this;
	}
	
	public List<AlterEntry> getAlredEntries() {
		return this.alterEntries;
	}
	
	public AlterEntry getAlterEntry(int index) {
		return this.alterEntries.get(index);
	}

	public String[] getAlterableParts() {
		return alterableParts;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public String getName() {
		return name;
	}
	
	public static void register(Configuration config){
		BendsLogger.info("Registering Animated Entities...");
		
		animatedEntities.clear();
		
		registerEntity(config, new AnimatedEntity("player", "Player", EntityPlayer.class, 
			new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager()),
			new String[] {"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
			"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg", "playerRotation",
			"leftItemRotation", "rightItemRotation"}).
			add(new net.gobbob.mobends.animation.player.Animation_Stand()).
			add(new net.gobbob.mobends.animation.player.Animation_Walk()).
			add(new net.gobbob.mobends.animation.player.Animation_Sneak()).
			add(new net.gobbob.mobends.animation.player.Animation_Sprint()).
			add(new net.gobbob.mobends.animation.player.Animation_Jump()).
			add(new net.gobbob.mobends.animation.player.Animation_Swimming()).
			add(new net.gobbob.mobends.animation.player.Animation_Climbing()).
			add(new net.gobbob.mobends.animation.player.Animation_Elytra()).
			add(new net.gobbob.mobends.animation.player.Animation_Guard()).
			add(new net.gobbob.mobends.animation.player.Animation_Bow()).
			add(new net.gobbob.mobends.animation.player.Animation_Riding()).
			add(new net.gobbob.mobends.animation.player.Animation_Mining()).
			add(new net.gobbob.mobends.animation.player.Animation_Attack()).
			add(new net.gobbob.mobends.animation.player.Animation_Axe()));
		registerEntity(config, new AnimatedEntity("zombie", "Zombie", EntityZombie.class,
			new RenderBendsZombie(Minecraft.getMinecraft().getRenderManager()),
			new String[] {"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
			"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}).
			add(new net.gobbob.mobends.animation.zombie.Animation_Stand()).
			add(new net.gobbob.mobends.animation.zombie.Animation_Walk()));
		registerEntity(config, new AnimatedEntity("husk", "Husk", EntityHusk.class,
				new RenderBendsHusk(Minecraft.getMinecraft().getRenderManager()),
				new String[] {"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
				"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}).
				add(new net.gobbob.mobends.animation.zombie.Animation_Stand()).
				add(new net.gobbob.mobends.animation.zombie.Animation_Walk()));
		registerEntity(config, new AnimatedEntity("spider", "Spider", EntitySpider.class,
			new RenderBendsSpider(Minecraft.getMinecraft().getRenderManager()),
			new String[] {"head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7", "leg8",
			"foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5", "foreLeg6", "foreLeg7", "foreLeg8"}).
			add(new net.gobbob.mobends.animation.spider.Animation_OnGround()).
			add(new net.gobbob.mobends.animation.spider.Animation_Jump()).
			add(new net.gobbob.mobends.animation.spider.Animation_WallClimb()));
		registerEntity(config, new AnimatedEntity("cave_spider", "Cave Spider", EntityCaveSpider.class,
			new RenderBendsCaveSpider(Minecraft.getMinecraft().getRenderManager()),
			new String[] {"head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7", "leg8",
			"foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5", "foreLeg6", "foreLeg7", "foreLeg8"}).
			add(new net.gobbob.mobends.animation.spider.Animation_OnGround()).
			add(new net.gobbob.mobends.animation.spider.Animation_Jump()).
			add(new net.gobbob.mobends.animation.spider.Animation_WallClimb()));
		registerEntity(config, new AnimatedEntity("skeleton", "Skeleton", EntitySkeleton.class,
			new RenderBendsSkeleton(Minecraft.getMinecraft().getRenderManager()),
			new String[] {"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
			"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}).
			add(new net.gobbob.mobends.animation.skeleton.Animation_Stand()).
			add(new net.gobbob.mobends.animation.skeleton.Animation_Walk()).
			add(new net.gobbob.mobends.animation.skeleton.Animation_Bow()).
			add(new net.gobbob.mobends.animation.skeleton.Animation_Attack()));
		registerEntity(config, new AnimatedEntity("wither_skeleton", "Wither Skeleton", EntityWitherSkeleton.class,
				new RenderBendsWitherSkeleton(Minecraft.getMinecraft().getRenderManager()),
				new String[] {"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
				"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}).
				add(new net.gobbob.mobends.animation.skeleton.Animation_Stand()).
				add(new net.gobbob.mobends.animation.skeleton.Animation_Walk()).
				add(new net.gobbob.mobends.animation.skeleton.Animation_Bow()).
				add(new net.gobbob.mobends.animation.skeleton.Animation_Attack()));
		registerEntity(config, new AnimatedEntity("stray", "Stray", EntityStray.class,
				new RenderBendsStray(Minecraft.getMinecraft().getRenderManager()),
				new String[] {"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
				"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg"}).
				add(new net.gobbob.mobends.animation.skeleton.Animation_Stand()).
				add(new net.gobbob.mobends.animation.skeleton.Animation_Walk()).
				add(new net.gobbob.mobends.animation.skeleton.Animation_Bow()).
				add(new net.gobbob.mobends.animation.skeleton.Animation_Attack()));
		
		RenderingRegistry.registerEntityRenderingHandler(EntitySpectralArrow.class, new RenderBendsSpectralArrow(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityTippedArrow.class, new RenderBendsTippedArrow(Minecraft.getMinecraft().getRenderManager()));
		
		playerRenderer = new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager());
		skinMap.put("default", playerRenderer);
		skinMap.put("slim", new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager(), true));
	}
	
	public static void registerEntity(Configuration config, AnimatedEntity argEntity){
		BendsLogger.info("Registering " + argEntity.displayName);
		for(int a = 0; a < argEntity.alterEntries.size(); a++){
			AlterEntry alterEntry = argEntity.alterEntries.get(a);
			alterEntry.setAnimate(config.get("Animated", alterEntry.getName(), true).getBoolean());
		}
		if(argEntity.alterEntries.get(0).isAnimated()) RenderingRegistry.registerEntityRenderingHandler(argEntity.entityClass, argEntity.renderer);
		animatedEntities.put(argEntity.name, argEntity);
	}
	
	public Animation getAnimation(String name){
		return (Animation) animations.get(name);
	}
	
	public static AnimatedEntity get(String name){
		return (AnimatedEntity) animatedEntities.get(name);
	}
	
	public static AnimatedEntity getByEntity(Entity argEntity){
		for (String key : animatedEntities.keySet()) {
			if(animatedEntities.get(key).entityClass.isInstance(argEntity)){
				return animatedEntities.get(key);
			}
		}
		return null;
	}

	public static RenderBendsPlayer getPlayerRenderer(AbstractClientPlayer player) {
		String s = ((AbstractClientPlayer)player).getSkinType();
		RenderBendsPlayer renderplayer = (RenderBendsPlayer)skinMap.get(s);
        return renderplayer != null ? renderplayer : playerRenderer;
	}
}
