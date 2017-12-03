package net.gobbob.mobends.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsSpider;
import net.gobbob.mobends.util.BendsLogger;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;

public class Data_Spider extends EntityData{
	public static List<Data_Spider> dataList = new ArrayList<Data_Spider>(); 
	
	public ModelRendererBends spiderHead;
    public ModelRendererBends spiderNeck;
    public ModelRendererBends spiderBody;
    
    public ModelRendererBends spiderLeg1;
    public ModelRendererBends spiderLeg2;
    public ModelRendererBends spiderLeg3;
    public ModelRendererBends spiderLeg4;
    public ModelRendererBends spiderLeg5;
    public ModelRendererBends spiderLeg6;
    public ModelRendererBends spiderLeg7;
    public ModelRendererBends spiderLeg8;
    
    public ModelRendererBends spiderForeLeg1;
    public ModelRendererBends spiderForeLeg2;
    public ModelRendererBends spiderForeLeg3;
    public ModelRendererBends spiderForeLeg4;
    public ModelRendererBends spiderForeLeg5;
    public ModelRendererBends spiderForeLeg6;
    public ModelRendererBends spiderForeLeg7;
    public ModelRendererBends spiderForeLeg8;
	
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    
	public Data_Spider(int argEntityID) {
		super(argEntityID);
	}
	
	public void syncModelInfo(ModelBendsSpider argModel){
		if(this.spiderHead == null) this.spiderHead = new ModelRendererBends(argModel); this.spiderHead.sync((ModelRendererBends)argModel.spiderHead);
		if(this.spiderNeck == null) this.spiderNeck = new ModelRendererBends(argModel); this.spiderNeck.sync((ModelRendererBends)argModel.spiderNeck);
		if(this.spiderBody == null) this.spiderBody = new ModelRendererBends(argModel); this.spiderBody.sync((ModelRendererBends)argModel.spiderBody);
		
		if(this.spiderLeg1 == null) this.spiderLeg1 = new ModelRendererBends(argModel); this.spiderLeg1.sync((ModelRendererBends)argModel.spiderLeg1);
		if(this.spiderLeg2 == null) this.spiderLeg2 = new ModelRendererBends(argModel); this.spiderLeg2.sync((ModelRendererBends)argModel.spiderLeg2);
		if(this.spiderLeg3 == null) this.spiderLeg3 = new ModelRendererBends(argModel); this.spiderLeg3.sync((ModelRendererBends)argModel.spiderLeg3);
		if(this.spiderLeg4 == null) this.spiderLeg4 = new ModelRendererBends(argModel); this.spiderLeg4.sync((ModelRendererBends)argModel.spiderLeg4);
		if(this.spiderLeg5 == null) this.spiderLeg5 = new ModelRendererBends(argModel); this.spiderLeg5.sync((ModelRendererBends)argModel.spiderLeg5);
		if(this.spiderLeg6 == null) this.spiderLeg6 = new ModelRendererBends(argModel); this.spiderLeg6.sync((ModelRendererBends)argModel.spiderLeg6);
		if(this.spiderLeg7 == null) this.spiderLeg7 = new ModelRendererBends(argModel); this.spiderLeg7.sync((ModelRendererBends)argModel.spiderLeg7);
		if(this.spiderLeg8 == null) this.spiderLeg8 = new ModelRendererBends(argModel); this.spiderLeg8.sync((ModelRendererBends)argModel.spiderLeg8);
		
		if(this.spiderForeLeg1 == null) this.spiderForeLeg1 = new ModelRendererBends(argModel); this.spiderForeLeg1.sync((ModelRendererBends)argModel.spiderForeLeg1);
		if(this.spiderForeLeg2 == null) this.spiderForeLeg2 = new ModelRendererBends(argModel); this.spiderForeLeg2.sync((ModelRendererBends)argModel.spiderForeLeg2);
		if(this.spiderForeLeg3 == null) this.spiderForeLeg3 = new ModelRendererBends(argModel); this.spiderForeLeg3.sync((ModelRendererBends)argModel.spiderForeLeg3);
		if(this.spiderForeLeg4 == null) this.spiderForeLeg4 = new ModelRendererBends(argModel); this.spiderForeLeg4.sync((ModelRendererBends)argModel.spiderForeLeg4);
		if(this.spiderForeLeg5 == null) this.spiderForeLeg5 = new ModelRendererBends(argModel); this.spiderForeLeg5.sync((ModelRendererBends)argModel.spiderForeLeg5);
		if(this.spiderForeLeg6 == null) this.spiderForeLeg6 = new ModelRendererBends(argModel); this.spiderForeLeg6.sync((ModelRendererBends)argModel.spiderForeLeg6);
		if(this.spiderForeLeg7 == null) this.spiderForeLeg7 = new ModelRendererBends(argModel); this.spiderForeLeg7.sync((ModelRendererBends)argModel.spiderForeLeg7);
		if(this.spiderForeLeg8 == null) this.spiderForeLeg8 = new ModelRendererBends(argModel); this.spiderForeLeg8.sync((ModelRendererBends)argModel.spiderForeLeg8);
		
		this.renderOffset.set(argModel.renderOffset);
		this.renderRotation.set(argModel.renderRotation);
	}
	
	public static void add(Data_Spider argData){
		dataList.add((Data_Spider) argData);
	}
	
	public static Data_Spider get(int argEntityID){
		for(int i = 0;i < dataList.size();i++){
			if(dataList.get(i).entityID == argEntityID){
				return dataList.get(i);
			}
		}
		
		Data_Spider newData = new Data_Spider(argEntityID);
		
		if(Minecraft.getMinecraft().theWorld.getEntityByID(argEntityID) != null){
			dataList.add((Data_Spider) newData);
		}
		
		return newData;
	}

	@Override
	public void update(float argPartialTicks) {
		super.update(argPartialTicks);
	}
	
	@Override
	public void onLiftoff(){
		super.onLiftoff();
	}
}