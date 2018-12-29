package net.gobbob.mobends.core.animatedentity;

import java.lang.reflect.InvocationTargetException;

import net.gobbob.mobends.core.client.model.entity.armor.ArmorModelFactory;
import net.gobbob.mobends.core.data.EntityDatabase;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.util.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class DefaultAlterEntry<T extends EntityLivingBase> extends AlterEntry<T>
{
	
	T previewEntity;
	
	public DefaultAlterEntry(String postfix, String unlocalizedName)
	{
		super(postfix, unlocalizedName);
	}
	
	public DefaultAlterEntry()
	{
		this("", null);
	}
	
	@Override
	void onRegistered(AnimatedEntity<T> owner)
	{
		super.onRegistered(owner);
	}

	@Override
	public LivingEntityData getDataForPreview()
	{
		if (previewEntity == null)
			this.previewEntity = this.createPreviewEntity();
		
		return EntityDatabase.instance.getOrMake(this.owner.getDataFactory(), previewEntity);
	}
	
}