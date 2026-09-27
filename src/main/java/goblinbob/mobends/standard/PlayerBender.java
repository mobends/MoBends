package goblinbob.mobends.standard;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import goblinbob.mobends.standard.client.renderer.entity.mutated.PlayerRenderer;
import goblinbob.mobends.standard.data.PlayerData;
import goblinbob.mobends.core.ModStatics;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import net.minecraft.client.entity.AbstractClientPlayer;

public class PlayerBender extends EntityBender<AbstractClientPlayer>
{

    public PlayerBender()
    {
        super(ModStatics.MODID, "player", "mobends.player", AbstractClientPlayer.class, new PlayerRenderer());
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
