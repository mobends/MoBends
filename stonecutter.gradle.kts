// The version whose code src/ currently holds. Switch it with the Stonecutter IDE plugin or the
// "Set active project to ..." Gradle tasks, never by hand, and switch back to the vcsVersion of
// settings.gradle.kts before committing (CI checks this).
stonecutter active "1.21.1-fabric"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    // Makes the version- and loader-specific sections of stonecutter.properties.toml apply.
    properties {
        tags(version, loader)
    }

    // `//? if fabric {`, `//? if neoforge {` and `//? if forge {`
    constants {
        match(loader, "fabric", "neoforge", "forge")
    }

    swaps["mod_version"] = "\"${properties["mod.version"]}\";"
    swaps["minecraft"] = "\"${current.version}\";"

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }

        string(current.parsed >= "26.1") {
            replace("classTweaker v2 named", "classTweaker v2 official")
        }

        // APIs renamed by 26.x, written out in full so nothing else matches.
        string(current.parsed >= "26.1") {
            replace(".getDirection().getNormal()", ".getDirection().getUnitVec3i()")
            replace("InputConstants.Type.KEYSYM", "InputConstants.Type.KEYBOARD")
            replace("FMLEnvironment.dist", "FMLEnvironment.getDist()")
            replace("import net.minecraft.world.item.UseAnim;", "import net.minecraft.world.item.ItemUseAnimation;")
            replace("UseAnim.", "ItemUseAnimation.")
            replace("UseAnim action", "ItemUseAnimation action")
            replace("UseAnim animation", "ItemUseAnimation animation")
            // The GUI's drawing context (it extracts what's drawn, later in the frame) and its text calls.
            replace("import net.minecraft.client.gui.GuiGraphics;", "import net.minecraft.client.gui.GuiGraphicsExtractor;")
            replace("GuiGraphics graphics", "GuiGraphicsExtractor graphics")
            replace("graphics.drawString(", "graphics.text(")
            replace("graphics.drawCenteredString(", "graphics.centeredText(")
        }

        // Classes that moved to other packages by 26.x (a mob's entity and model classes went into a
        // package of their own). Whole import lines, so no longer name that starts the same matches.
        string(current.parsed >= "26.1") {
            replace("import net.minecraft.client.model.BeeModel;", "import net.minecraft.client.model.animal.bee.BeeModel;")
            replace("import net.minecraft.client.model.ChickenModel;", "import net.minecraft.client.model.animal.chicken.ChickenModel;")
            replace("import net.minecraft.client.model.CreeperModel;", "import net.minecraft.client.model.monster.creeper.CreeperModel;")
            replace("import net.minecraft.client.model.FoxModel;", "import net.minecraft.client.model.animal.fox.FoxModel;")
            replace("import net.minecraft.client.model.HoglinModel;", "import net.minecraft.client.model.monster.hoglin.HoglinModel;")
            replace("import net.minecraft.client.model.HorseModel;", "import net.minecraft.client.model.animal.equine.HorseModel;")
            replace("import net.minecraft.client.model.IronGolemModel;", "import net.minecraft.client.model.animal.golem.IronGolemModel;")
            replace("import net.minecraft.client.model.PlayerModel;", "import net.minecraft.client.model.player.PlayerModel;")
            replace("import net.minecraft.client.model.ShulkerModel;", "import net.minecraft.client.model.monster.shulker.ShulkerModel;")
            replace("import net.minecraft.client.model.SkeletonModel;", "import net.minecraft.client.model.monster.skeleton.SkeletonModel;")
            replace("import net.minecraft.client.model.SpiderModel;", "import net.minecraft.client.model.monster.spider.SpiderModel;")
            replace("import net.minecraft.client.model.SquidModel;", "import net.minecraft.client.model.animal.squid.SquidModel;")
            replace("import net.minecraft.client.model.TadpoleModel;", "import net.minecraft.client.model.animal.frog.TadpoleModel;")
            replace("import net.minecraft.client.model.VillagerModel;", "import net.minecraft.client.model.npc.VillagerModel;")
            replace("import net.minecraft.client.model.WitchModel;", "import net.minecraft.client.model.monster.witch.WitchModel;")
            replace("import net.minecraft.client.model.WolfModel;", "import net.minecraft.client.model.animal.wolf.WolfModel;")
            replace("import net.minecraft.client.model.ZombieModel;", "import net.minecraft.client.model.monster.zombie.ZombieModel;")
            replace("import net.minecraft.client.model.ZombieVillagerModel;", "import net.minecraft.client.model.monster.zombie.ZombieVillagerModel;")
            replace("import net.minecraft.client.renderer.RenderType;", "import net.minecraft.client.renderer.rendertype.RenderType;")
            replace("import net.minecraft.client.resources.PlayerSkin;", "import net.minecraft.world.entity.player.PlayerSkin;")
            replace("import net.minecraft.Util;", "import net.minecraft.util.Util;")
            replace("import net.minecraft.world.entity.animal.Chicken;", "import net.minecraft.world.entity.animal.chicken.Chicken;")
            replace("import net.minecraft.world.entity.animal.Cow;", "import net.minecraft.world.entity.animal.cow.Cow;")
            replace("import net.minecraft.world.entity.animal.IronGolem;", "import net.minecraft.world.entity.animal.golem.IronGolem;")
            replace("import net.minecraft.world.entity.animal.MushroomCow;", "import net.minecraft.world.entity.animal.cow.MushroomCow;")
            replace("import net.minecraft.world.entity.animal.Pig;", "import net.minecraft.world.entity.animal.pig.Pig;")
            replace("import net.minecraft.world.entity.animal.PolarBear;", "import net.minecraft.world.entity.animal.polarbear.PolarBear;")
            replace("import net.minecraft.world.entity.animal.Sheep;", "import net.minecraft.world.entity.animal.sheep.Sheep;")
            replace("import net.minecraft.world.entity.animal.Squid;", "import net.minecraft.world.entity.animal.squid.Squid;")
            replace("import net.minecraft.world.entity.animal.Wolf;", "import net.minecraft.world.entity.animal.wolf.Wolf;")
            replace("import net.minecraft.world.entity.monster.Skeleton;", "import net.minecraft.world.entity.monster.skeleton.Skeleton;")
            replace("import net.minecraft.world.entity.monster.Spider;", "import net.minecraft.world.entity.monster.spider.Spider;")
            replace("import net.minecraft.world.entity.monster.Zombie;", "import net.minecraft.world.entity.monster.zombie.Zombie;")
            replace("import net.minecraft.world.entity.monster.ZombieVillager;", "import net.minecraft.world.entity.monster.zombie.ZombieVillager;")
            replace("import net.minecraft.world.entity.monster.ZombifiedPiglin;", "import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;")
            replace("import net.minecraft.world.entity.npc.Villager;", "import net.minecraft.world.entity.npc.villager.Villager;")
            replace("import net.minecraft.world.entity.projectile.AbstractArrow;", "import net.minecraft.world.entity.projectile.arrow.AbstractArrow;")
        }
    }
}
