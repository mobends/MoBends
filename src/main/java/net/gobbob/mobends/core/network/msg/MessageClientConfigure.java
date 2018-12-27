package net.gobbob.mobends.core.network.msg;

import io.netty.buffer.ByteBuf;
import net.gobbob.mobends.core.network.NetworkConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageClientConfigure implements IMessage {
	boolean allowModelScaling;
	
    public MessageClientConfigure() {}
    public MessageClientConfigure(boolean allowModelScaling) {
        this.allowModelScaling = allowModelScaling;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	this.allowModelScaling = ByteBufUtils.readTag(buf).getBoolean("AllowScaling");
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	NBTTagCompound tag = new NBTTagCompound();
    	tag.setBoolean("AllowScaling", this.allowModelScaling);
        ByteBufUtils.writeTag(buf,tag);
    }

    public static class Handler implements IMessageHandler<MessageClientConfigure, IMessage> {
        
        @Override
        public IMessage onMessage(MessageClientConfigure message, MessageContext ctx) {
        	System.out.println("Recieved Mo' Bends server configuration. (" + (message.allowModelScaling) + ")");
        	NetworkConfiguration.instance.allowModelScaling = message.allowModelScaling;
            return null;
        }
    }
}