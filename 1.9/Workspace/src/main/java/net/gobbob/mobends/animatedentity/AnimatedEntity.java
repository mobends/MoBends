package net.gobbob.mobends.animatedentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.animatedentity.alterentry.AlterEntryWitherSkeleton;
import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.renderer.entity.RenderBendsPlayer;
import net.gobbob.mobends.client.renderer.entity.RenderBendsSkeleton;
import net.gobbob.mobends.client.renderer.entity.RenderBendsSpider;
import net.gobbob.mobends.client.renderer.entity.RenderBendsZombie;
import net.gobbob.mobends.util.BendsLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class AnimatedEntity {
	public static List<AnimatedEntity> animatedEntities = new ArrayList<AnimatedEntity>();
	
	public static Map skinMap = Maps.newHashMap();
    public static RenderBendsPlayer playerRenderer;
	
	public String id;
	public String displayName;
	
	public Class<? extends Entity> entityClass;
	public Render renderer;
	
	public List<Animation> animations = new ArrayList<Animation>();
	private List<AlterEntry> alterEntries = new ArrayList<AlterEntry>();
	
	public AnimatedEntity(String argID, String argDisplayName, Class<? extends Entity> argClass, Render argRenderer){
		this.id = argID;
		this.displayName = argDisplayName;
		this.entityClass = argClass;
		this.renderer = argRenderer;
		this.addAlterEntry(new AlterEntry(this.id, this.displayName));
	}
	
	public AnimatedEntity add(Animation argGroup){
		this.animations.add(argGroup);
		return this;
	}
	
	public static void register(Configuration config){
		BendsLogger.log("Registering Animated Entities...", BendsLogger.INFO);
		
		animatedEntities.clear();
		
		registerEntity(new AnimatedEntity("player","Player",EntityPlayer.class,new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager())).
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
		registerEntity(new AnimatedEntity("zombie","Zombie",EntityZombie.class,new RenderBendsZombie(Minecraft.getMinecraft().getRenderManager())).
			add(new net.gobbob.mobends.animation.zombie.Animation_Stand()).
			add(new net.gobbob.mobends.animation.zombie.Animation_Walk()));
		registerEntity(new AnimatedEntity("spider","Spider",EntitySpider.class,new RenderBendsSpider(Minecraft.getMinecraft().getRenderManager())).
			add(new net.gobbob.mobends.animation.spider.Animation_OnGround()).
			add(new net.gobbob.mobends.animation.spider.Animation_Jump()).
			add(new net.gobbob.mobends.animation.spider.Animation_WallClimb()));
		registerEntity(new AnimatedEntity("skeleton","Skeleton",EntitySkeleton.class,new RenderBendsSkeleton(Minecraft.getMinecraft().getRenderManager())).
			add(new net.gobbob.mobends.animation.skeleton.Animation_Stand()).
			add(new net.gobbob.mobends.animation.skeleton.Animation_Walk()).
			add(new net.gobbob.mobends.animation.skeleton.Animation_Bow()).
			add(new net.gobbob.mobends.animation.skeleton.Animation_Attack()).
			addAlterEntry(new AlterEntryWitherSkeleton("witherSkeleton", "Wither Skeleton")));
		
		for(int i = 0;i < AnimatedEntity.animatedEntities.size();i++){
			for(int a = 0; a < AnimatedEntity.animatedEntities.get(i).alterEntries.size(); a++){
				AlterEntry alterEntry = AnimatedEntity.animatedEntities.get(i).alterEntries.get(a);
				alterEntry.setAnimate(config.get("Animated", alterEntry.id, true).getBoolean());
			}
		}
		
		for(int i = 0;i < animatedEntities.size();i++){
			if(AnimatedEntity.animatedEntities.get(i).alterEntries.get(0).isAnimated()) RenderingRegistry.registerEntityRenderingHandler(animatedEntities.get(i).entityClass, animatedEntities.get(i).renderer);
		}
		
		playerRenderer = new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager());
		skinMap.put("default", playerRenderer);
		skinMap.put("slim", new RenderBendsPlayer(Minecraft.getMinecraft().getRenderManager(), true));
	}
	
	public static void registerEntity(AnimatedEntity argEntity){
		BendsLogger.log("Registering " + argEntity.displayName, BendsLogger.INFO);
		animatedEntities.add(argEntity);
	}
	
	public Animation get(String argName){
		for(int i = 0;i < animations.size();i++){
			if(animations.get(i).getName().equalsIgnoreCase(argName)){
				return animations.get(i);
			}
		}
		return null;
	}
	
	public static AnimatedEntity getByEntity(Entity argEntity){
		for(int i = 0;i < animatedEntities.size();i++){
			if(animatedEntities.get(i).entityClass.isInstance(argEntity)){
				return animatedEntities.get(i);
			}
		}
		return null;
	}

	public static RenderBendsPlayer getPlayerRenderer(AbstractClientPlayer player) {
		String s = ((AbstractClientPlayer)player).getSkinType();
		RenderBendsPlayer renderplayer = (RenderBendsPlayer)skinMap.get(s);
        return renderplayer != null ? renderplayer : playerRenderer;
	}
	
	public List<AlterEntry> getAlredEntries() {
		return this.alterEntries;
	}
	
	public AnimatedEntity addAlterEntry(AlterEntry alterEntry) {
		this.alterEntries.add(alterEntry);
		return this;
	}
	
	public AlterEntry getAlterEntry(int index) {
		return this.alterEntries.get(index);
	}
}
