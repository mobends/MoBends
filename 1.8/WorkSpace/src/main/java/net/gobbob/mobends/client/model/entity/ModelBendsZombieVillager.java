package net.gobbob.mobends.client.model.entity;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.minecraft.client.model.ModelRenderer;

public class ModelBendsZombieVillager extends ModelBendsZombie{
	public ModelBendsZombieVillager()
    {
        this(0.0f, 0.0f, false);
    }
    
    public ModelBendsZombieVillager(float p_i1167_1_, float p_i1167_2_, boolean p_i1165_3_)
    {
        super(p_i1167_1_, 0.0F, 64, p_i1165_3_ ? 32 : 64);
        
		/*if (p_i1165_3_)
        {
            this.bipedHead = new ModelRendererBends(this, 0, 0);
            this.bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 6, 8, p_i1167_1_);
            this.bipedHead.setRotationPoint(0.0F, 0.0F + p_i1167_2_, 0.0F);
        }
        else
        {
            this.bipedHead = new ModelRendererBends(this);
            this.bipedHead.setRotationPoint(0.0F, 0.0F + p_i1167_2_, 0.0F);
            this.bipedHead.setTextureOffset(0, 32).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, p_i1167_1_);
            this.bipedHead.setTextureOffset(24, 32).addBox(-1.0F, -3.0F, -6.0F, 2, 4, 2, p_i1167_1_);
        }*/
	}
}
