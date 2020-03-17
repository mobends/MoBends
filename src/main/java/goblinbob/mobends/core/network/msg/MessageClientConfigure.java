package goblinbob.mobends.core.network.msg;

import io.netty.buffer.ByteBuf;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.network.NetworkConfiguration;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageClientConfigure implements IMessage
{

    private static final String ALLOW_MODEL_SCALING_KEY = "allowModelScaling";

    public boolean allowModelScaling;

    /**
     * Necessary empty constructor, because of dynamic instancing.
     */
    public MessageClientConfigure() {}

    public MessageClientConfigure(boolean allowModelScaling)
    {
        this.allowModelScaling = allowModelScaling;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        if (tag == null)
        {
            Core.LOG.severe("An error occurred while receiving server configuration.");
            this.allowModelScaling = false;
            return;
        }
        this.allowModelScaling = tag.getBoolean(ALLOW_MODEL_SCALING_KEY);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(ALLOW_MODEL_SCALING_KEY, this.allowModelScaling);
        ByteBufUtils.writeTag(buf, tag);
    }

    public static class Handler implements IMessageHandler<MessageClientConfigure, IMessage>
    {

        @Override
        public IMessage onMessage(MessageClientConfigure message, MessageContext ctx)
        {
            Core.LOG.info("Received Mo' Bends server configuration.");
            Core.LOG.info(String.format(" - allowModelScaling: %b", message.allowModelScaling));

            NetworkConfiguration.instance.provideServerConfig(message);
            return null;
        }

    }

}