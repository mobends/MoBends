package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

public class TorchHoldingAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "torch_holding" };
	
	@Override
	public String[] getActions(BipedEntityData<?> data)
	{
		return ACTIONS;
	}
	
	@Override
	public void perform(BipedEntityData<?> data)
	{
		EntityLivingBase living = data.getEntity();
		EnumHandSide torchHand;
		ItemStack mainStack = living.getHeldItem(EnumHand.MAIN_HAND);
		ItemStack offStack = living.getHeldItem(EnumHand.OFF_HAND);
		
		if (mainStack != null && mainStack.getItem() == Item.getItemFromBlock(Blocks.TORCH))
		{
			torchHand = living.getPrimaryHand();
		}
		else if (offStack != null && offStack.getItem() == Item.getItemFromBlock(Blocks.TORCH))
		{
			torchHand = living.getPrimaryHand() == EnumHandSide.RIGHT ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
		}
		else
		{
			return;
		}
		
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = torchHand == EnumHandSide.RIGHT ? 1 : -1;
		IModelPart mainArm = torchHand == EnumHandSide.RIGHT ? data.rightArm : data.leftArm;
		IModelPart mainForeArm = torchHand == EnumHandSide.RIGHT ? data.rightForeArm : data.leftForeArm;
		
		mainArm.getRotation().orientX(-90.0F + data.headPitch.get() * 0.5F)
							 .rotateY(data.headYaw.get() * 0.7F);
		mainForeArm.getRotation().orientX(-5.0F);
	}
}
