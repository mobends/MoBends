package net.gobbob.mobends.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.vector.Vector2f;

import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animatedentity.alterentry.AlterEntry;
import net.gobbob.mobends.client.ClientProxy;
import net.gobbob.mobends.pack.BendsAction;
import net.gobbob.mobends.pack.BendsAction.Calculation;
import net.gobbob.mobends.pack.BendsAction.EnumBoxProperty;
import net.gobbob.mobends.pack.BendsAction.EnumModifier;
import net.gobbob.mobends.pack.BendsAction.EnumOperator;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.BendsTarget;
import net.gobbob.mobends.pack.BendsVar;
import net.gobbob.mobends.settings.SettingsBoolean;
import net.gobbob.mobends.settings.SettingsNode;
import net.gobbob.mobends.util.Color;
import net.gobbob.mobends.util.Draw;
import net.gobbob.mobends.util.EnumAxis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class GuiMBMenu extends GuiScreen{
	/*--VISUAL--*/
	public static final ResourceLocation menuTitleTexture = new ResourceLocation(MoBends.MODID,"textures/gui/menuTitle.png");
	public static final ResourceLocation displayBGTexture = new ResourceLocation(MoBends.MODID,"textures/gui/displayBG.png");
	public static final ResourceLocation puzzle_animation = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_animation.png");
	public static final ResourceLocation puzzle_model = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_model.png");
	public static final ResourceLocation puzzle_action_set = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_action_set.png");
	public static final ResourceLocation puzzle_action_add = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_action_add.png");
	public static final ResourceLocation puzzle_action_substract = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_action_substract.png");
	public static final ResourceLocation puzzle_action_multiply = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_action_multiply.png");
	public static final ResourceLocation puzzle_mod = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_mod.png");
	
	public static final ResourceLocation puzzle_add = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_add.png");
	public static final ResourceLocation puzzle_delete = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_delete.png");
	
	public static final ResourceLocation puzzle_rot = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_rot.png");
	public static final ResourceLocation puzzle_scale = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_scale.png");
	public static final ResourceLocation puzzle_prerot = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_prerot.png");
	
	public static final ResourceLocation puzzle_mod_none = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_mod_none.png");
	public static final ResourceLocation puzzle_mod_cos = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_mod_cos.png");
	public static final ResourceLocation puzzle_mod_sin = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_mod_sin.png");
	public static final ResourceLocation puzzle_mod_none_selected = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_mod_none_selected.png");
	public static final ResourceLocation puzzle_mod_cos_selected = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_mod_cos_selected.png");
	public static final ResourceLocation puzzle_mod_sin_selected = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_mod_sin_selected.png");
	
	public static final ResourceLocation puzzle_calc_add = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_calc_add.png");
	public static final ResourceLocation puzzle_calc_substract = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_calc_substract.png");
	public static final ResourceLocation puzzle_calc_set = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_calc_set.png");
	public static final ResourceLocation puzzle_calc_multiply = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_calc_multiply.png");
	public static final ResourceLocation puzzle_calc_add_selected = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_calc_add_selected.png");
	public static final ResourceLocation puzzle_calc_substract_selected = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_calc_substract_selected.png");
	public static final ResourceLocation puzzle_calc_set_selected = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_calc_set_selected.png");
	public static final ResourceLocation puzzle_calc_multiply_selected = new ResourceLocation(MoBends.MODID,"textures/gui/puzzle_calc_multiply_selected.png");
	
	
	public float titleTransitionState = 0.0f;
	public boolean titleTransition = true;
	
	public float[] buttonPositions = new float[AnimatedEntity.animatedEntities.size()];
	public float buttonRevealState = 0.0f;
	public float leftBgState = 0.0f;
	public float presetWindowState = 0.0f;
	public float previewRotation = 0.0f;
	
	/*--TECHNICAL--*/
	public boolean customizeWindow;
	public boolean settingsWindow;
	public boolean packsWindow;
	
	public List<AlterEntry> alterEntities = new ArrayList<AlterEntry>();
	public int currentAlterEntry = 0;
	EntityLivingBase portraitEntity;
	
	public int custom_currentAction = 0;
	public int custom_currentChange = 0;
	
	public float scroll_x = 0, scroll_y = 0;
	public boolean scrolling_x = false, scrolling_y = false;
	
	public GuiTextField custom_AnimationNameText,
						custom_PackTitle,
						custom_ModelNameText,
						custom_CalcValueText;
	
	private GuiListPacksSelection packsList;
	
	public GuiMBMenu(){
		Keyboard.enableRepeatEvents(true);
		this.titleTransition = true;
		this.titleTransitionState = 0.0f;
		
		for(int i = 0;i < AnimatedEntity.animatedEntities.size();i++){
			this.alterEntities.addAll(AnimatedEntity.animatedEntities.get(i).getAlredEntries());
		}
		
		this.buttonPositions = new float[this.alterEntities.size()];
		
		this.packsList = new GuiListPacksSelection(this, this.mc, this.width, this.height, 32, this.height - 64);
	}

	public void initGui()
    {
		super.initGui();
		this.buttonList.clear();
		
		if(this.customizeWindow | this.settingsWindow | this.packsWindow)
		this.buttonList.add(new GuiButton(0,this.packsWindow ? width-70 : 10,height-30,60,20,"Back"));
		
		if(!this.customizeWindow & !this.settingsWindow & !this.packsWindow){
			this.buttonList.add(new GuiButton(1,-90+(int)(this.leftBgState*100),height-30,60,20,"Settings"));
			this.buttonList.add(new GuiButton(3,this.width-(int)(this.leftBgState*100)+30,height-30,60,20,"Packs"));
		}
		if(this.settingsWindow){
			for(int i = 0;i < SettingsNode.settings.length;i++){
				if(SettingsNode.settings[i] instanceof SettingsBoolean){
					this.buttonList.add(new GuiToggleButton(10+i,(int)(this.width+this.presetWindowState*-500.0f+20),50+i*25,((SettingsBoolean)SettingsNode.settings[i]).data).setTitle(SettingsNode.settings[i].displayName, 100));
				}
			}
		}
		
		if(this.customizeWindow){
			AlterEntry alterEntry = this.alterEntities.get(this.currentAlterEntry);
			this.buttonList.add(new GuiToggleButton(2,(int)(this.width+this.presetWindowState*-500.0f+10),35+128,alterEntry.isAnimated()).setTitle("Animate", 128-40));
			if(this.getCurrentAction() != null){
				this.buttonList.add(new GuiButton(4,(int)(this.getModelSelectionLoc().x+this.getModelSelectionSize().x-40),(int)this.getModelSelectionLoc().y+95,20,20,"+"));
				GuiButton minus = new GuiButton(5,(int)(this.getModelSelectionLoc().x+this.getModelSelectionSize().x-20),(int)this.getModelSelectionLoc().y+95,20,20,"-");
				minus.enabled = this.getCurrentAction().calculations.size() > 0;
				this.buttonList.add(minus);
			}
		}
		
		for(int i = 0;i < this.alterEntities.size();i++){
			this.buttonList.add(new GuiButton(100+i, (int) (buttonPositions[i]-80+presetWindowState*-100), 70+i*25, 80, 20, this.alterEntities.get(i).getDisplayName()));
		}
		
		if(this.custom_AnimationNameText == null){
			this.custom_AnimationNameText = new GuiTextField(0,this.fontRendererObj, (int)(this.getModelSelectionLoc().x+5), (int)(this.getModelSelectionLoc().y+5+10), (int)(this.getModelSelectionSize().x-10), 15);
			if(this.getCurrentAction() != null) this.custom_AnimationNameText.setText(this.getCurrentAction().anim);
		}
		else{
			this.custom_AnimationNameText.xPosition = (int)(this.getModelSelectionLoc().x+5);
			this.custom_AnimationNameText.yPosition = (int)(this.getModelSelectionLoc().y+5+10);
		}
		
		if(this.custom_ModelNameText == null){
			this.custom_ModelNameText = new GuiTextField(0,this.fontRendererObj, (int)(this.getModelSelectionLoc().x+5), (int)(this.getModelSelectionLoc().y+5+10+15+5+10), (int)(this.getModelSelectionSize().x-10), 15);
			if(this.getCurrentAction() != null) this.custom_ModelNameText.setText(this.getCurrentAction().model);
		}
		else{
			this.custom_ModelNameText.xPosition = (int)(this.getModelSelectionLoc().x+5);
			this.custom_ModelNameText.yPosition = (int)(this.getModelSelectionLoc().y+5+10+15+5+10);
		}
		
		if(this.custom_CalcValueText == null){
			this.custom_CalcValueText = new GuiTextField(0,this.fontRendererObj, (int)(this.getModelSelectionLoc().x+5), (int)(this.getModelSelectionLoc().y+(5+10+15+5+10)*2+10+5+10+5), (int)(this.getModelSelectionSize().x-10-32), 15);
			if(this.getCurrentAction() != null && this.getCurrentCalculation() != null) this.custom_CalcValueText.setText(getCurrentCalculation().globalVar != null ? getCurrentCalculation().globalVar : String.valueOf(getCurrentCalculation().number));
			this.custom_CalcValueText.setCursorPositionZero();
		}
		else{
			this.custom_CalcValueText.width = (int)(this.getModelSelectionSize().x-10-32);
			this.custom_CalcValueText.xPosition = (int)(this.getModelSelectionLoc().x+5+32);
			this.custom_CalcValueText.yPosition = (int)(this.getModelSelectionLoc().y+(5+10+15+5+10)*2+10+5+10+5);
		}
		
		if(this.custom_PackTitle == null){
			this.custom_PackTitle = new GuiTextField(0,this.fontRendererObj, (int)(this.getActionWindowX()+5), 25+10+3, 150, 14);
			if(BendsPack.currentPack == 0) this.custom_PackTitle.setText("Default");
			else this.custom_PackTitle.setText(BendsPack.getCurrentPack().displayName);
			this.custom_PackTitle.setCursorPositionZero();
		}
		else{
			this.custom_PackTitle.width = 150;
			this.custom_PackTitle.height = 14;
			this.custom_PackTitle.xPosition = (int) (this.getActionWindowX()+5);
			this.custom_PackTitle.yPosition = 25+10+3;
		}
    }
	
	protected void keyTyped(char par1, int par2) {
		switch(par2){
			case 1:
				this.close();
			break;
		}
		
		if(this.customizeWindow){
			if (this.custom_AnimationNameText.isFocused())
	        {
	            this.custom_AnimationNameText.textboxKeyTyped(par1, par2);
	            this.assignAnimationToCurrentAction(this.custom_AnimationNameText.getText());
	        }
			else if (this.custom_ModelNameText.isFocused())
	        {
	            this.custom_ModelNameText.textboxKeyTyped(par1, par2);
	            BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(this.custom_currentAction).model = this.custom_ModelNameText.getText();
	        }
			else if (this.custom_CalcValueText.isFocused())
	        {
	            this.custom_CalcValueText.textboxKeyTyped(par1, par2);
	            this.assignCalcValue(custom_CalcValueText.getText());
	        }
			else if(this.custom_PackTitle.isFocused()){
	        	this.custom_PackTitle.textboxKeyTyped(par1, par2);
	        	if(BendsPack.currentPack == 0){
	        		createANewPack(this.custom_PackTitle.getText());
	        	}
	        	BendsPack.getCurrentPack().displayName = this.custom_PackTitle.getText();
	        }
		}
	}
	
	public void close(){
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
	
	@Override
	public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        MoBends.saveConfig();
        if(BendsPack.currentPack != 0){
	        try {
				BendsPack.getCurrentPack().save();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
	
	@Override
    public void updateScreen() {
		initGui();
		this.previewRotation+=2;
		
		int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        
        if(presetWindowState > 0.0f & customizeWindow){
        	this.custom_AnimationNameText.updateCursorCounter();
        	this.custom_ModelNameText.updateCursorCounter();
        	this.custom_CalcValueText.updateCursorCounter();
        	this.custom_PackTitle.updateCursorCounter();
        	
        	if(mouseY > 25+10+25 & mouseY < 25+10+25+getActionWindowHeight()){
				String varAnim = "";
		    	int displayIndex = 0;
		    	if(BendsPack.getTargetByID(getCurrentAlterEntry().id) != null){
		    		int i = 0;
		    		for(;i < BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.size();i++){
			    		BendsAction action = BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(i);
			    		if(!action.anim.equalsIgnoreCase(varAnim)){
			    			if(displayIndex > 0){
				    			displayIndex++;
			    			}
			    			varAnim = action.anim;
			    			displayIndex++;
			    		}
			    		
			    		if(mouseX >= getActionWindowX()+10 &
			    			mouseY >= 25+25+10+5+displayIndex*18+getYScrollAmount() & mouseY <= 25+25+10+5+displayIndex*18+18+getYScrollAmount()){
			    			if(action.visual_DeletePopUp < 1)
			    				action.visual_DeletePopUp+=0.2f;
			    		}else{
			    			if(action.visual_DeletePopUp >0)
			    				action.visual_DeletePopUp-=0.2f;
			    		}
			    		displayIndex++;
			    	}
		    		displayIndex++;
		    	}
	        }
        }
        
        if(this.scrolling_y){
        	this.scroll_y=(mouseY-(25+10+25))/getActionWindowHeight()*(getActualActionWindowHeight()/getActionWindowHeight());
        	if(this.scroll_y > 1)
        		this.scroll_y = 1;
        	if(this.scroll_y < 0)
        		this.scroll_y = 0;
        }
        
        if(!Mouse.isButtonDown(0)){
        	this.scrolling_x = false;
        	this.scrolling_y = false;
        }
	}
	
	@Override
	protected void mouseClicked(int x, int y, int p_73864_3_)
    {
		if(packsWindow){
			if(this.packsList != null){
				if(this.packsList.mouseClicked(x, y, p_73864_3_)) {
					int i = this.packsList.getSelectedIndex();
					if(i != BendsPack.currentPack && BendsPack.currentPack != 0){
						try {
							BendsPack.getCurrentPack().save();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					BendsPack.currentPack = i;
					this.custom_PackTitle.setText(BendsPack.getCurrentPack().displayName);
					try {
						BendsPack.getCurrentPack().apply();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if(presetWindowState > 0.0f & customizeWindow){
			this.custom_AnimationNameText.mouseClicked(x,y,p_73864_3_);
			this.custom_ModelNameText.mouseClicked(x,y,p_73864_3_);
			this.custom_PackTitle.mouseClicked(x,y,p_73864_3_);
			
			if(getActualActionWindowHeight() > getActionWindowHeight()){
				if(x > getActionWindowX()+500-128-20-10 & x < getActionWindowX()+500-128-20-10+10 &
				   y > 25+10+25 & y < 25+10+25+getActionWindowHeight()){
					this.scrolling_y = true;
				}
			}
			if(this.getCurrentAction() != null){
				if(y > this.custom_ModelNameText.yPosition+15+5+10 & y < this.custom_ModelNameText.yPosition+15+5+10+10){
					if(x > getModelSelectionLoc().x+5 & x < getModelSelectionLoc().x+5+18){
						this.getCurrentAction().mod = null;
					}
					if(x > getModelSelectionLoc().x+5+18 & x < getModelSelectionLoc().x+5+18*2){
						this.getCurrentAction().mod = EnumModifier.COS;
					}
					if(x > getModelSelectionLoc().x+5+18*2 & x < getModelSelectionLoc().x+5+18*3){
						this.getCurrentAction().mod = EnumModifier.SIN;
					}
				}
				if(this.getCurrentCalculation() != null){
					if(y > this.custom_ModelNameText.yPosition+(15+5+10)*2 & y < this.custom_ModelNameText.yPosition+(15+5+10)*2+10){
						if(x > getModelSelectionLoc().x+5 & x < getModelSelectionLoc().x+5+10){
							this.getCurrentCalculation().operator = EnumOperator.ADD;
						}
						if(x > getModelSelectionLoc().x+5+10 & x < getModelSelectionLoc().x+5+10*2){
							this.getCurrentCalculation().operator = EnumOperator.SUBSTRACT;
						}
						if(x > getModelSelectionLoc().x+5+10*2 & x < getModelSelectionLoc().x+5+10*3){
							this.getCurrentCalculation().operator = EnumOperator.MULTIPLY;
						}
						if(x > getModelSelectionLoc().x+5+10*3 & x < getModelSelectionLoc().x+5+10*4){
							this.getCurrentCalculation().operator = EnumOperator.SET;
						}
					}
					this.custom_CalcValueText.mouseClicked(x,y,p_73864_3_);
				}
			}
			
			if(BendsPack.getTargetByID(getCurrentAlterEntry().id) == null || BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.size() <= 0){
				if(x >= getActionWindowX() & x <= getActionWindowX()+16 &
				   y >= 25+25+10+5+getYScrollAmount() & y <= 25+25+10+5+getYScrollAmount()+16){
					if(BendsPack.currentPack == 0){
						createANewPack("Untitled");
					}
					addNewDefaultAction("all");
				}
			}
			if(y > 25+10+25 & y < 25+10+25+getActionWindowHeight()){
				String varAnim = "";
		    	int displayIndex = 0;
		    	if(BendsPack.getTargetByID(getCurrentAlterEntry().id) != null){
		    		int i = 0;
		    		
		    		for(;i < BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.size();i++){
			    		BendsAction action = BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(i);
			    		if(!action.anim.equalsIgnoreCase(varAnim)){
			    			if(displayIndex > 0){
			    				if(x >= getActionWindowX()+10 & x <= getActionWindowX()+10+16 & 
			    				   y >= 25+25+10+5+displayIndex*18+getYScrollAmount() & y <= 25+25+10+5+displayIndex*18+getYScrollAmount()+16){
			    					addNewDefaultAction(varAnim);
			    				}
				    			displayIndex++;
			    			}
			    			varAnim = action.anim;
			    			displayIndex++;
			    		}
			    		
			    		if(action != null){
			    			if(x > getActionWindowX()+10 &
			    			   y > 25+25+10+5+displayIndex*18+getYScrollAmount() &
			    			   y < 25+25+10+5+displayIndex*18+16+getYScrollAmount()){
			    				
			    				if(x > getActionWindowX()+10 & x < getActionWindowX()+10+20){
			    					if(action.prop == EnumBoxProperty.PREROT)
			    						action.prop = EnumBoxProperty.ROT;
			    					else if(action.prop == EnumBoxProperty.ROT)
			    						action.prop = EnumBoxProperty.SCALE;
			    					else if(action.prop == EnumBoxProperty.SCALE)
			    						action.prop = EnumBoxProperty.PREROT;
			    				}
			    				
			    				if(x > getActionWindowX()+10+20 & x < getActionWindowX()+10+31){
			    					if(action.axis == null)
			    						action.axis = EnumAxis.X;
			    					else if(action.axis == EnumAxis.X)
			    						action.axis = EnumAxis.Y;
			    					else if(action.axis == EnumAxis.Y)
			    						action.axis = EnumAxis.Z;
			    					else if(action.axis == EnumAxis.Z)
			    						action.axis = null;
			    				}
			    				this.custom_currentAction = i;
			    				this.custom_currentChange = 0;
			    				this.custom_AnimationNameText.setText(BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(this.custom_currentAction).anim);
			    				this.custom_ModelNameText.setText(BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(this.custom_currentAction).model);
			    				
			    				if(getCurrentCalculation() != null){
				    				this.custom_CalcValueText.setText(getCurrentCalculation().globalVar != null ? getCurrentCalculation().globalVar : String.valueOf(getCurrentCalculation().number));
				    				this.custom_CalcValueText.setCursorPositionZero();
			    				}
			    				
			    				for(int s = 0;s < action.calculations.size();s++){
					    			Calculation calculation = action.calculations.get(s);
				    				
				    				Minecraft.getMinecraft().renderEngine.bindTexture(calculation.operator == EnumOperator.ADD ? puzzle_action_add : calculation.operator == EnumOperator.SUBSTRACT ? puzzle_action_substract : calculation.operator == EnumOperator.SET ? puzzle_action_set : puzzle_action_multiply);
				    				if(x > getActionWindowX()+10+31+64-7+(action.mod != null ? 32-7 : 0)+s*(64-7) &
				    				   x < getActionWindowX()+10+31+64-7+(action.mod != null ? 32-7 : 0)+s*(64-7)+64-7){
				    					this.custom_currentChange = s;
				    					this.custom_CalcValueText.setText(getCurrentCalculation().globalVar != null ? getCurrentCalculation().globalVar : String.valueOf(getCurrentCalculation().number));
					    				this.custom_CalcValueText.setCursorPositionZero();
				    				}
				    			}
			    				
			    				
			    				if(x > getActionWindowX()+10+31+64-7+(action.mod != null ? 32-7 : 0)+action.calculations.size()*(64-7) &
			 		    		   x < getActionWindowX()+10+31+64-7+(action.mod != null ? 32-7 : 0)+action.calculations.size()*(64-7)+32){
				    				BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.remove(i);
				    				if(i <= this.custom_currentAction){
				    					if(this.custom_currentAction > 0) this.custom_currentAction--;
				    				}
				    				i--;
			    				}
			    			}
			    		}
			    		displayIndex++;
			    	}
		    		if(x >= getActionWindowX()+10 & x <= getActionWindowX()+10+16 & 
		    	       y >= 25+25+10+5+displayIndex*18+getYScrollAmount() & y <= 25+25+10+5+displayIndex*18+getYScrollAmount()+16){
		    			this.addNewDefaultAction(varAnim);
		    		}
		    		displayIndex++;
		    	}
			}
        }
		
		try {
			super.mouseClicked(x, y, p_73864_3_);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        if(this.packsList != null) this.packsList.mouseReleased(mouseX, mouseY, state);
    }
	
	public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        if(this.packsList != null) this.packsList.handleMouseInput();
    }
	
	public void createANewPack(String string) {
		BendsPack newPack = new BendsPack();
		newPack.filename = null;
		newPack.displayName = string;
		newPack.author = Minecraft.getMinecraft().thePlayer.getName();
		newPack.description = "A custom pack made by " + Minecraft.getMinecraft().thePlayer.getName() + ".";
		BendsPack.bendsPacks.add(newPack);
		BendsPack.currentPack = BendsPack.bendsPacks.size()-1;
		this.custom_PackTitle.setText(newPack.displayName);
	}

	protected void actionPerformed(GuiButton par1GuiButton)
    {
    	if(par1GuiButton.id >= 100){
    		this.currentAlterEntry = par1GuiButton.id-100;
    		this.customizeWindow = true;
    		this.portraitEntity = this.getCurrentAlterEntry().getID() == "player" ? Minecraft.getMinecraft().thePlayer : this.getCurrentAlterEntry().getEntity();
    	}else if(par1GuiButton.id >= 10){
    		int s = 0;
    		for(int i = 0;i < SettingsNode.settings.length;i++){
    			if(SettingsNode.settings[i] instanceof SettingsBoolean){
    				if(s == par1GuiButton.id-10){
    					((SettingsBoolean)SettingsNode.settings[i]).data = !((SettingsBoolean)SettingsNode.settings[i]).data;
    					break;
    				}
    				s++;
    			}
    		}
    	}
        switch(par1GuiButton.id){
        	case 0:
        		this.customizeWindow = false;
        		this.settingsWindow = false;
        		this.packsWindow = false;
        	break;
        	case 1:
        		this.settingsWindow = true;
        	break;
        	case 2:
        		this.getCurrentAlterEntry().toggleAnimated();
        	break;
        	case 3:
        		this.packsWindow = true;
        	break;
        	case 4:
        		this.getCurrentAction().calculations.add(new Calculation(EnumOperator.SET,0));
        		this.custom_currentChange = this.getCurrentAction().calculations.size()-1;
        	break;
        	case 5:
        		this.getCurrentAction().calculations.remove(this.getCurrentCalculation());
        		this.custom_currentChange = this.custom_currentChange-1;
        		if(this.custom_currentChange < 0) custom_currentChange = 0;
        	break;
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
    	GL11.glDisable(GL11.GL_LIGHTING);
    	GL11.glEnable(GL11.GL_BLEND);
    	
    	if(this.titleTransition){
    		this.titleTransitionState+=(128-this.titleTransitionState)/5;
    	}else{
    		this.titleTransitionState+=(100-this.titleTransitionState)/7;
    		
    		for(float i = 0;i < buttonPositions.length;i++){
    			if(buttonRevealState >= i/buttonPositions.length){
    				buttonPositions[(int)i] += (90-buttonPositions[(int)i])/4;
    			}
    		}
    		
    		buttonRevealState+=0.05f;
    		leftBgState+=(1.0f-leftBgState)/4.0f;
    	}
    	
    	if(this.titleTransitionState > 126){
    		this.titleTransition = false;
    	}
    	
    	if(customizeWindow | settingsWindow | packsWindow){
    		this.presetWindowState+=(1.0f-this.presetWindowState)/4.0f;
    	}else{
    		this.presetWindowState+=(0.0f-this.presetWindowState)/4.0f;
    	}
    	
    	GL11.glPushMatrix();
    		Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
    		Draw.rectangle_xgradient(-leftBgState*-100.0f-100.0f+presetWindowState*-100,0,100, height, new Color(0,0,0,0.7f), new Color(0,0,0,0.3f));
    		GL11.glColor4f(0,0,0,0.5f);
    		Draw.rectangle(-leftBgState*-105.0f-105.0f+presetWindowState*-105+100,0,5, height);
    	GL11.glPopMatrix();
    	
    	GL11.glColor3f(1.0f,1.0f,1.0f);
    	this.mc.renderEngine.bindTexture(menuTitleTexture);
    	Draw.rectangle(width/2-64+this.presetWindowState*(-this.width/2+80),titleTransitionState-100,128,64);
	    
    	if(presetWindowState > 0.0f){
    		GL11.glPushMatrix();
	    		Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
	    		GL11.glColor4f(0,0,0,0.5f);
	    		float varWidth = packsWindow ? 250 : 500;
	    		Draw.rectangle(this.width+this.presetWindowState*-varWidth,0,varWidth, 20);
	    		Draw.rectangle(this.width+this.presetWindowState*-varWidth,25,varWidth, height);
	    	GL11.glPopMatrix();
    	}
    	
    	if(presetWindowState > 0.0f & customizeWindow){
    		displayCustomizeWindow();
    	}
    	
    	if(presetWindowState > 0.0f & settingsWindow){
    		String title = "Settings";
    		this.drawString(this.fontRendererObj, title, (int) (this.width+this.presetWindowState*-500.0f+250-this.fontRendererObj.getStringWidth(title)/2), 5, 0xffffff);
    	}
    	
    	if(presetWindowState > 0.0f & packsWindow){
    		String title = "Packs";
    		this.drawString(this.fontRendererObj, title, (int) (this.width+this.presetWindowState*-250.0f+125-this.fontRendererObj.getStringWidth(title)/2), 5, 0xffffff);

    		if(this.packsList != null) this.packsList.drawScreen(mouseX, mouseY, partialTicks);
    	}
    	
    	int color = 47121212;
    	
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    public void renderLivingEntity(int argX, int argY, int scale, EntityLivingBase par5EntityLivingBase)
    {
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)argX, (float)argY, 50.0F);
        GL11.glScalef((float)(-scale), (float)scale, (float)scale);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(this.previewRotation, 0.0F, 1.0F, 0.0F);
        float f2 = par5EntityLivingBase.renderYawOffset;
        float f3 = par5EntityLivingBase.rotationYaw;
        float f4 = par5EntityLivingBase.rotationPitch;
        float f5 = par5EntityLivingBase.prevRotationYawHead;
        float f6 = par5EntityLivingBase.rotationYawHead;
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glColor3f(1,1,1);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        //GL11.glRotatef(-((float)Math.atan((double)(par4 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        par5EntityLivingBase.renderYawOffset = 0;
        par5EntityLivingBase.rotationYaw = 0;
        par5EntityLivingBase.rotationPitch = 0;
        par5EntityLivingBase.rotationYawHead = par5EntityLivingBase.rotationYaw;
        par5EntityLivingBase.prevRotationYawHead = par5EntityLivingBase.rotationYaw;
        //GL11.glTranslatef(0.0F, (float) par5EntityLivingBase.getYOffset(), 0.0F);
        Minecraft.getMinecraft().getRenderManager().playerViewY = 180.0F;
        par5EntityLivingBase.moveForward=1.0f;
        Minecraft.getMinecraft().getRenderManager().doRenderEntity(par5EntityLivingBase, 0.0D, 0.0D, 0.0D, 0.0F, 1.0f, false);
        //Minecraft.getMinecraft().getRenderManager().renderEntityStatic(par5EntityLivingBase, 0.0f, true);
        par5EntityLivingBase.renderYawOffset = f2;
        par5EntityLivingBase.rotationYaw = f3;
        par5EntityLivingBase.rotationPitch = f4;
        par5EntityLivingBase.prevRotationYawHead = f5;
        par5EntityLivingBase.rotationYawHead = f6;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
    }
    
    public boolean doesGuiPauseGame()
    {
        return true;
    }
	
	public void displayCustomizeWindow(){
		GL11.glColor4f(1,1,1,1);
		String title = "Animation Customization";
		this.drawCenteredString(this.fontRendererObj, title, (int) (this.width+this.presetWindowState*-500.0f+250), 5, 0xffffff);
		
		//Display Title BG Section
		GL11.glPushMatrix();
    		GL11.glColor4f(1.0f,1.0f,1.0f,1.0f);
        	this.mc.renderEngine.bindTexture(displayBGTexture);
        	Draw.rectangle(this.width+this.presetWindowState*-500.0f+10,35,128,128);
		GL11.glPopMatrix();
		
		renderLivingEntity((int)(this.width+this.presetWindowState*-500.0f+10+64),150,50, this.portraitEntity);
    	
    	String warning = "* to see the changes after toggling the animations ON or OFF, restart your game.";
    	this.drawString(this.fontRendererObj, warning, (int) (this.width+this.presetWindowState*-500.0f+20), this.height-20, 0xffffff);
    	
    	//Display Model Selection Window
    	if(true){
	    	GL11.glPushMatrix();
	    		Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
	    		GL11.glColor4f(0,0,0,0.5f);
	    		Draw.rectangle(getModelSelectionLoc().x,getModelSelectionLoc().y,getModelSelectionSize().x, getModelSelectionSize().y);
	    	GL11.glPopMatrix();
	    	
	    	if(getCurrentAction() != null){
	    		this.drawString(this.fontRendererObj, "Animation:", this.custom_AnimationNameText.xPosition, this.custom_AnimationNameText.yPosition-10, 0xffffff);
	        	this.drawString(this.fontRendererObj, "Model:", this.custom_ModelNameText.xPosition, this.custom_ModelNameText.yPosition-10, 0xffffff);
	        	this.drawString(this.fontRendererObj, "Modifier:", this.custom_ModelNameText.xPosition, this.custom_ModelNameText.yPosition-10+15+5+10, 0xffffff);
	        	
	        	this.custom_AnimationNameText.drawTextBox();
	        	this.custom_ModelNameText.drawTextBox();
	        	
	        	GL11.glColor4f(1,1,1,1);
	        	
		    	GL11.glPushMatrix();
					Minecraft.getMinecraft().renderEngine.bindTexture(getCurrentAction().mod == null ? puzzle_mod_none_selected : puzzle_mod_none);
					Draw.rectangle(this.getModelSelectionLoc().x+5,this.custom_ModelNameText.yPosition+15+5+10,32,16);
				GL11.glPopMatrix();
		    	
				GL11.glPushMatrix();
					Minecraft.getMinecraft().renderEngine.bindTexture(getCurrentAction().mod == EnumModifier.COS ? puzzle_mod_cos_selected : puzzle_mod_cos);
					Draw.rectangle(this.getModelSelectionLoc().x+5+18,this.custom_ModelNameText.yPosition+15+5+10,32,16);
				GL11.glPopMatrix();
				
				GL11.glPushMatrix();
					Minecraft.getMinecraft().renderEngine.bindTexture(getCurrentAction().mod == EnumModifier.SIN ? puzzle_mod_sin_selected : puzzle_mod_sin);
					Draw.rectangle(this.getModelSelectionLoc().x+5+18*2,this.custom_ModelNameText.yPosition+15+5+10,32,16);
				GL11.glPopMatrix();
				
				this.drawString(this.fontRendererObj, "Calculation:", this.custom_ModelNameText.xPosition, this.custom_ModelNameText.yPosition-10+(15+5+10)*2, 0xffffff);
				if(this.getCurrentCalculation() != null){
		        	this.drawString(this.fontRendererObj, "Value:", this.custom_ModelNameText.xPosition, this.custom_ModelNameText.yPosition+15+5+(15+5+10)*2-1, this.isValidCalcValue(custom_CalcValueText.getText()) ? 0xffffff : 0xff0000);
		        	
		        	GL11.glPushMatrix();
						Minecraft.getMinecraft().renderEngine.bindTexture(getCurrentCalculation().operator == EnumOperator.ADD ? puzzle_calc_add_selected : puzzle_calc_add);
						Draw.rectangle(this.getModelSelectionLoc().x+5,this.custom_ModelNameText.yPosition+(15+5+10)*2,16,16);
					GL11.glPopMatrix();
					GL11.glPushMatrix();
						Minecraft.getMinecraft().renderEngine.bindTexture(getCurrentCalculation().operator == EnumOperator.SUBSTRACT ? puzzle_calc_substract_selected : puzzle_calc_substract);
						Draw.rectangle(this.getModelSelectionLoc().x+5+10,this.custom_ModelNameText.yPosition+(15+5+10)*2,16,16);
					GL11.glPopMatrix();
					GL11.glPushMatrix();
						Minecraft.getMinecraft().renderEngine.bindTexture(getCurrentCalculation().operator == EnumOperator.MULTIPLY ? puzzle_calc_multiply_selected : puzzle_calc_multiply);
						Draw.rectangle(this.getModelSelectionLoc().x+5+10*2,this.custom_ModelNameText.yPosition+(15+5+10)*2,16,16);
					GL11.glPopMatrix();
					GL11.glPushMatrix();
						Minecraft.getMinecraft().renderEngine.bindTexture(getCurrentCalculation().operator == EnumOperator.SET ? puzzle_calc_set_selected : puzzle_calc_set);
						Draw.rectangle(this.getModelSelectionLoc().x+5+10*3,this.custom_ModelNameText.yPosition+(15+5+10)*2,16,16);
					GL11.glPopMatrix();
		        	
					this.custom_CalcValueText.drawTextBox();
				}
	    	}
    	}
    	
    	GL11.glPushMatrix();
    		GL11.glEnable(GL11.GL_BLEND);
    		Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
    		GL11.glColor4f(0,0,0,0.7f);
    		Draw.rectangle(getActionWindowX(),25+10,500-128-20-10, 20);
    		Draw.rectangle(getActionWindowX(),25+10+25,500-128-20-10, getActionWindowHeight());
    	GL11.glPopMatrix();
    	
    	if(getActualActionWindowHeight() > getActionWindowHeight()){
	    	GL11.glPushMatrix();
				Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
				GL11.glColor4f(1,1,1,1);
				Draw.rectangle(getActionWindowX()+500-128-20-10,25+10+25+scroll_y*(getActualActionWindowHeight()-getActionWindowHeight())*(getActionWindowHeight()/getActualActionWindowHeight()),10, getActionWindowHeight()*(getActionWindowHeight()/getActualActionWindowHeight()));
			GL11.glPopMatrix();
    	}
    	
    	this.custom_PackTitle.drawTextBox();
    	
    	float scale = (Minecraft.getMinecraft().displayWidth/this.width);
    	
    	GL11.glViewport((int) (Minecraft.getMinecraft().displayWidth+(int)(this.presetWindowState*-500+10+128+10)*scale), (int)(30*scale), (int)((500-128-20-10)*scale), (int)(Minecraft.getMinecraft().displayHeight+(-25-40-25)*scale));
    	GL11.glMatrixMode(GL11.GL_PROJECTION);
    	GL11.glLoadIdentity();
    	GL11.glOrtho(this.width+this.presetWindowState*-500+10+128+10, this.width+this.presetWindowState*-500+10+128+10+500-128-20-10, 25+10+25+height-25-40-25, 25+10+25, 1000.0D, 3000.0D);
    	GL11.glMatrixMode(GL11.GL_MODELVIEW);
    	String varAnim = "";
    	int displayIndex = 0;
    	if(BendsPack.getTargetByID(getCurrentAlterEntry().id) != null && BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.size() > 0){
    		int i = 0;
    		for(;i < BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.size();i++){
	    		BendsAction action = BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(i);
	    		if(!action.anim.equalsIgnoreCase(varAnim)){
	    			GL11.glColor4f(1,1,1,1);
	    			
	    			if(displayIndex > 0){
	    				Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_add);
		    			Draw.rectangle(getActionWindowX()+10,25+25+10+5+displayIndex*18+getYScrollAmount(),16,16);
		    			displayIndex++;
	    			}
	    			
	    			Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_animation);
	    			Draw.rectangle(getActionWindowX(),25+25+10+5+displayIndex*18+getYScrollAmount(),128, 16);
	    			this.drawString(this.fontRendererObj, action.anim,(int)(getActionWindowX()+5),(int)(25+25+10+5+4+displayIndex*18+getYScrollAmount()), 0xffffff);
	    			
	    			varAnim = action.anim;
	    			displayIndex++;
	    		}
	    		
	    		if(this.custom_currentAction == i){
		    		GL11.glPushMatrix();
						GL11.glTranslatef(0,0,0);
						Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.texture_NULL);
						Draw.rectangle_xgradient(getActionWindowX()+10-5,25+25+10+5+displayIndex*18+getYScrollAmount()-1,31+64-7+(action.mod != null ? 32-7 : 0)+action.calculations.size()*(64-7)-16-16+(16+7)*action.visual_DeletePopUp+50+5, 18,new Color(1,0.7f,0.2f,0.2f),new Color(1,0.7f,0.2f,1));
					GL11.glPopMatrix();
	    		}
	    		
    			GL11.glColor4f(1,1,1,1);
    			
	    		Minecraft.getMinecraft().renderEngine.bindTexture(action.prop == EnumBoxProperty.ROT ? puzzle_rot : action.prop == EnumBoxProperty.SCALE ? puzzle_scale : puzzle_prerot);
	    		Draw.rectangle(getActionWindowX()+10,25+25+10+5+displayIndex*18+getYScrollAmount(),32, 16);
	    		
	    		if(action != null){
	    			Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_delete);
	    			Draw.rectangle(getActionWindowX()+10+31+64-7+(action.mod != null ? 32-7 : 0)+action.calculations.size()*(64-7)-16-16+(16+7)*action.visual_DeletePopUp,25+25+10+5+displayIndex*18+getYScrollAmount(),32, 16);
	    			
	    			this.drawString(this.fontRendererObj, action.axis == EnumAxis.X ? "x" : action.axis == EnumAxis.Y ? "y" : action.axis == EnumAxis.Z ? "z" : "?" ,(int)(getActionWindowX()+10+22),(int)(25+25+10+5+4+displayIndex*18+getYScrollAmount()), 0xffffff);
			    	Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_model);
			    	Draw.rectangle(getActionWindowX()+10+31,25+25+10+5+displayIndex*18+getYScrollAmount(),64, 16);
			    	this.drawString(this.fontRendererObj, action.model,(int)(getActionWindowX()+5+10+31),(int)(25+25+10+5+4+displayIndex*18+getYScrollAmount()), 0xffffff);
	    			
	    			if(action.mod != null){
	    				Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_mod);
	    				Draw.rectangle(getActionWindowX()+10+31+64-7,25+25+10+5+displayIndex*18+getYScrollAmount(),32,16);
		    			this.drawString(this.fontRendererObj, action.mod == EnumModifier.COS ? "cos" : "sin",(int)(getActionWindowX()+10+31+64-7+5),(int)(25+25+10+5+4+displayIndex*18+getYScrollAmount()), 0xffffff);
	    			}
	    			for(int s = 0;s < action.calculations.size();s++){
		    			Calculation calculation = action.calculations.get(s);
	    				
	    				Minecraft.getMinecraft().renderEngine.bindTexture(calculation.operator == EnumOperator.ADD ? puzzle_action_add : calculation.operator == EnumOperator.SUBSTRACT ? puzzle_action_substract : calculation.operator == EnumOperator.SET ? puzzle_action_set : puzzle_action_multiply);
	    				GL11.glPushMatrix();
	    					if(this.custom_currentChange != s | this.custom_currentAction != i) GL11.glColor4f(0.7f,0.7f,0.7f,1);
	    					else GL11.glColor4f(1,1,1,1);
	    					Draw.rectangle(getActionWindowX()+10+31+64-7+(action.mod != null ? 32-7 : 0)+s*(64-7),25+25+10+5+displayIndex*18+getYScrollAmount(),64, 16);
		    			GL11.glPopMatrix();
	    				this.drawString(this.fontRendererObj, ""+(calculation.globalVar != null ? calculation.globalVar : calculation.number),(int)(getActionWindowX()+10+31+64-7+(action.mod != null ? 32-7 : 0))+s*(64-7)+20,(int)(25+25+10+5+4+displayIndex*18+getYScrollAmount()), 0xffffff);
	    			}
	    		}
	    		displayIndex++;
	    	}
    		Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_add);
    		Draw.rectangle(getActionWindowX()+10,25+25+10+5+displayIndex*18+getYScrollAmount(),16, 16);
    		displayIndex++;
    	}else{
    		GL11.glPushMatrix();
	    		GL11.glColor4f(1,1,1,1);
	    		Minecraft.getMinecraft().renderEngine.bindTexture(puzzle_add);
	    		Draw.rectangle(getActionWindowX(),25+25+10+5+displayIndex*18+getYScrollAmount(),16, 16);	
    		GL11.glPopMatrix();
    	}
    	GL11.glViewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    	GL11.glMatrixMode(GL11.GL_PROJECTION);
    	GL11.glLoadIdentity();
    	ScaledResolution res = new ScaledResolution(mc);
    	GL11.glOrtho(0, res.getScaledWidth(), res.getScaledHeight(), 0, 1000.0D, 3000.0D);
    	GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
	
	public float getActualActionWindowHeight(){
		String varAnim = "";
	    int displayIndex = 0;
	    if(BendsPack.getTargetByID(getCurrentAlterEntry().id) != null){
	    		int i = 0;
	    		for(;i < BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.size();i++){
		    		BendsAction action = BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(i);
		    		if(!action.anim.equalsIgnoreCase(varAnim)){
		    			if(displayIndex > 0){
			    			displayIndex++;
		    			}
		    			varAnim = action.anim;
		    			displayIndex++;
		    		}
		    		displayIndex++;
		    	}
	    		displayIndex++;
	    }
	    displayIndex++;
		return displayIndex*18;
	}
	
	public float getActionWindowHeight(){
		return height-25-40-25;
	}
	
	public float getYScrollAmount(){
		return -this.scroll_y*(getActualActionWindowHeight()-getActionWindowHeight());
	}
	
	public float getActionWindowX(){
		return getPropertiesWindowX()+10+128+10+5;
	}
	
	public float getPropertiesWindowX(){
		return this.width+this.presetWindowState*-getPropertiesWindowWidth();
	}
	
	public float getPropertiesWindowWidth(){
		return packsWindow ? 250 : 500;
	}
	
	public Vector2f getModelSelectionLoc(){
		return new Vector2f(getPropertiesWindowX()+10,200);
	}
	
	public Vector2f getModelSelectionSize(){
		return new Vector2f(128, 200);
	}
	
	public void assignAnimationToCurrentAction(String argAnim){
		BendsTarget target = BendsPack.getTargetByID(getCurrentAlterEntry().id);
		if(target == null)
			return;
		BendsAction action = target.actions.get(this.custom_currentAction);
		if(!argAnim.equalsIgnoreCase(action.anim)){
			target.actions.remove(this.custom_currentAction);
			action.anim = argAnim;
			int newIndex = 0;
			List<BendsAction> newActionList = new ArrayList<BendsAction>();
			boolean done = false;
			boolean properAnim = false;
			for(int i = 0;i < target.actions.size();i++){
				if(target.actions.get(i).anim.equalsIgnoreCase(argAnim)){
					properAnim = true;
				}else{
					if(properAnim & !done){
						newActionList.add(action);
						done = true;
						newIndex = i;
					}
				}
				
				newActionList.add(target.actions.get(i));
			}
			if(!done){
				newActionList.add(action);
				newIndex = newActionList.size()-1;
			}
			target.actions = newActionList;
			this.custom_currentAction = newIndex;
		}
	}
	
	public boolean isValidCalcValue(String value){
		if(value == null | value.isEmpty()){
			return false;
		}
		
		if(NumberUtils.isNumber(value)){
			return true;
		}else{
			if(BendsVar.getGlobalVar(value) != Float.POSITIVE_INFINITY){
				return true;
			}
		}
		return false;
	}
	
	public void assignCalcValue(String value){
		if(isValidCalcValue(value)){
			if(this.getCurrentCalculation() != null){
				if(NumberUtils.isNumber(value)){
					this.getCurrentCalculation().globalVar = null;
					this.getCurrentCalculation().number = Float.valueOf(value);
				}else{
					this.getCurrentCalculation().globalVar = value;
				}
			}
		}else{
			this.getCurrentCalculation().globalVar = null;
			this.getCurrentCalculation().number = 0;
		}
	}
	
	public BendsAction getCurrentAction(){
		
		if(BendsPack.getTargetByID(getCurrentAlterEntry().id) == null)
			return null;
		
		if(this.custom_currentAction < BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.size())
			return BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(this.custom_currentAction);
		else
			return null;
	}
	
	public BendsAction.Calculation getCurrentCalculation(){
		if(getCurrentAction() == null || getCurrentAction().calculations.size() <= 0)
			return null;
		
		return getCurrentAction().calculations.get(this.custom_currentChange);
	}
	
	public void addNewDefaultAction(String argAnim) {
		if(BendsPack.getTargetByID(getCurrentAlterEntry().id) == null){
			BendsPack.targets.add(new BendsTarget(getCurrentAlterEntry().id));
		}
		BendsAction action = new BendsAction(argAnim,"",EnumBoxProperty.ROT,null,0.3f,1);
		BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.add(action);
		this.custom_currentAction = BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.size()-1;
		action.anim = "NULL";
		this.assignAnimationToCurrentAction(argAnim);
		if(this.custom_currentAction > 0){
			action.model = BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(this.custom_currentAction-1).model;
			action.prop = BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(this.custom_currentAction-1).prop;
			action.axis = BendsPack.getTargetByID(getCurrentAlterEntry().id).actions.get(this.custom_currentAction-1).axis;
		}
	}
	
	public AlterEntry getCurrentAlterEntry() {
		return this.alterEntities.get(this.currentAlterEntry);
	}
}