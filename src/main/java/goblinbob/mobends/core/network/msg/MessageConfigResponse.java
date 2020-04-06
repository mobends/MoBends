package goblinbob.mobends.core.network.msg;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.network.NetworkConfiguration;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * This message is sent by the server to a client as a response
 * to a {@link MessageConfigRequest} message.
 */
public class MessageConfigResponse implements IMessage
{

    private static final String ALLOW_MODEL_SCALING_KEY = "allowModelScaling";

    public boolean allowModelScaling;

    /**
     * Necessary empty constructor, because of dynamic instancing.
     */
    public MessageConfigResponse() {}

    public MessageConfigResponse(boolean allowModelScaling)
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

    public static class Handler implements IMessageHandler<MessageConfigResponse, IMessage>
    {

        @Override
        public IMessage onMessage(MessageConfigResponse message, MessageContext ctx)
        {
            Core.LOG.info("Received Mo' Bends server configuration.");
            Core.LOG.info(String.format(" - allowModelScaling: %b", message.allowModelScaling));

            NetworkConfiguration.instance.provideServerConfig(message);
            return null;
        }

    }

}