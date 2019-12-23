package net.gobbob.mobends.standard;

import net.gobbob.mobends.core.bender.EntityBender;
import net.gobbob.mobends.core.bender.IPreviewer;
import net.gobbob.mobends.core.data.IEntityDataFactory;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.mutators.IMutatorFactory;
import net.gobbob.mobends.standard.client.renderer.entity.mutated.PlayerRenderer;
import net.gobbob.mobends.standard.data.PlayerData;
import net.gobbob.mobends.standard.main.ModStatics;
import net.gobbob.mobends.standard.mutators.PlayerMutator;
import net.gobbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.entity.AbstractClientPlayer;

public class PlayerBender extends EntityBender<AbstractClientPlayer>
{

    private PlayerPreviewer previewer;
    private String[] alterableParts = {
        "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg", "rightLeg",
        "leftForeLeg", "rightForeLeg", "totalRotation", "leftItemRotation", "rightItemRotation"
    };

    public PlayerBender()
    {
        super(ModStatics.MODID, "player", "mobends.player", AbstractClientPlayer.class, new PlayerRenderer());
        this.previewer = new PlayerPreviewer();
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

    @Override
    public IPreviewer<?> getPreviewer()
    {
        return previewer;
    }

    @Override
    public LivingEntityData<?> getDataForPreview()
    {
        return PlayerPreviewer.getPreviewData();
    }

}
