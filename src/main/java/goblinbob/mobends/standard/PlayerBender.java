package goblinbob.mobends.standard;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import goblinbob.mobends.standard.client.renderer.entity.mutated.PlayerRenderer;
import goblinbob.mobends.standard.data.PlayerData;
import goblinbob.mobends.standard.main.ModStatics;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import net.minecraft.client.entity.AbstractClientPlayer;

public class PlayerBender extends EntityBender<AbstractClientPlayer>
{

    private String[] alterableParts = {
        "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg", "rightLeg",
        "leftForeLeg", "rightForeLeg", "totalRotation", "leftItemRotation", "rightItemRotation"
    };

    public PlayerBender()
    {
        super(ModStatics.MODID, "player", "mobends.player", AbstractClientPlayer.class, new PlayerRenderer());
    }

    @Override
    public String[] getAlterableParts()
    {
        return alterableParts;
    }

    @Override
    public IEntityDataFactory<AbstractClientPlayer> getDataFactory()
    {
        return PlayerData::new;
    }

    @Override
    public IMutatorFactory<AbstractClientPlayer> getMutatorFactory()
    {
        return PlayerMutator::new;
    }

}
