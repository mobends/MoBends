package goblinbob.mobends.standard;

import goblinbob.mobends.core.addon.AddonAnimationRegistry;
import goblinbob.mobends.core.addon.IAddon;
import goblinbob.mobends.core.client.definition.DefinedLayers;
import goblinbob.mobends.standard.client.renderer.entity.SwordTrail;
import goblinbob.mobends.standard.client.renderer.entity.layers.PlayerAccessoriesLayer;
import goblinbob.mobends.standard.client.renderer.entity.layers.WolfMiscLayer;
import goblinbob.mobends.standard.data.CapeWave;
import goblinbob.mobends.standard.kumo.CapeDriver;
import goblinbob.mobends.standard.kumo.MoBendsOperations;
import goblinbob.mobends.standard.kumo.SwordTrailDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsTemplate;
import goblinbob.mobends.standard.kumo.spider.SpiderLegs;
import goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsTemplate;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
//? if <26.1 {
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
//?}
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
//? if >=26.1 {
/*import net.minecraft.client.renderer.entity.layers.SheepWoolLayer;
import net.minecraft.client.renderer.entity.layers.SheepWoolUndercoatLayer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
*///?} else {
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
//?}
//? if >=1.21 {
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
//?}

/** Mo' Bends' own content, registered as an addon like any other mod's (1.12's {@code DefaultAddon}). */
public class DefaultAddon implements IAddon {
    @Override
    public void registerContent(AddonAnimationRegistry registry) {
        // The mobs themselves are data: the type files in bends/types/ give each its model definition
        // (bends/models/).

        // The operations and drivers the animators name; an animator naming one that isn't
        // registered fails to load.
        MoBendsOperations.register();
        registry.registerDriver("cape", CapeDriver::create, CapeDriver.Template.class);
        registry.registerDriver("sword_trail", SwordTrailDriver::create, SwordTrailDriver.Template.class);
        registry.registerDriver("spider_idle_legs", SpiderIdleLegsDriver::create, SpiderIdleLegsTemplate.class);
        registry.registerDriver("spider_moving_legs", SpiderMovingLegsDriver::create, SpiderMovingLegsTemplate.class);

        // What the definitions' "components" can name: where a spider's feet are planted, the
        // sword trail, the cape's ripple.
        registry.registerComponent("spider_legs", SpiderLegs::new);
        registry.registerComponent("sword_trail", data -> new SwordTrail());
        registry.registerComponent("cape_wave", data -> new CapeWave(data.getEntity()));

        // What the definitions' "layers" can name (see DefinedLayers, BendsLayers): mobends:armor,
        // mobends:held_item, ...
        registry.registerLayer("armor", HumanoidArmorLayer.class);
        registry.registerLayer("held_item", ItemInHandLayer.class);
        registry.registerLayer("custom_head", CustomHeadLayer.class);
        registry.registerLayer("cape", CapeLayer.class);
        //? if >=26.1 {
        /*registry.registerLayer("elytra", net.minecraft.client.renderer.entity.layers.WingsLayer.class);
        *///?} else {
        registry.registerLayer("elytra", ElytraLayer.class);
        //?}
        // Mo' Bends' own layer of supporter accessories (added to the player's renderer by PlayerRendererMixin).
        registry.registerLayer("accessories", PlayerAccessoriesLayer.class);
        // Mo' Bends' own layer of the wolf's mouth (added to the wolf's renderer by WolfRendererMixin).
        registry.registerLayer("wolf_misc", WolfMiscLayer.class);
        // Layers that draw their own copy of the model: drawn bent, as 1.12's AnimatedCopyLayer drew them.
        registry.registerCopyLayer("creeper_charge", CreeperPowerLayer.class);
        //? if >=26.1 {
        /*registry.registerCopyLayer("sheep_wool", SheepWoolLayer.class);
        *///?} else {
        registry.registerCopyLayer("sheep_wool", SheepFurLayer.class);
        //?}
        // The drowned's outer layer, the wolf's armour and the pig's saddle are copies of their
        // model that no 1.12 definition names (the first two are new; the pig only bends with the
        // dev pack), and an unbent copy would show the mob twice.
        registry.registerImplicitLayer("drowned_outer_layer", DrownedOuterLayer.class, DefinedLayers.Role.COPY);
        //? if >=1.21 {
        registry.registerImplicitLayer("wolf_armor_layer", WolfArmorLayer.class, DefinedLayers.Role.COPY);
        //?}
        //? if >=26.1 {
        /*// 26.x draws the saddle (and the other worn equipment of a mob, a horse's, a strider's) with
        // a layer for any equipment, and the coloured sheep's undercoat with a layer of its own: a
        // copy of the mob's model each, drawn bent only if it is one (BendsLayers.isCopy).
        registry.registerImplicitLayer("saddle_layer", SimpleEquipmentLayer.class, DefinedLayers.Role.COPY);
        registry.registerImplicitLayer("sheep_wool_undercoat", SheepWoolUndercoatLayer.class, DefinedLayers.Role.COPY);
        *///?} else {
        registry.registerImplicitLayer("saddle_layer", SaddleLayer.class, DefinedLayers.Role.COPY);
        //?}
    }

    @Override
    public String getDisplayName() {
        return "Default";
    }
}
