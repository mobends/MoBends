package goblinbob.mobends.core.network.msg;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.client.AnimationPolicy;
import goblinbob.mobends.core.network.NetworkConfiguration;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
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

    /** The configuration read on the network thread, applied on the client's. */
    private NBTTagCompound received;

    /**
     * Necessary empty constructor, because of dynamic instancing.
     */
    public MessageConfigResponse() {}

    @Override
    public void toBytes(ByteBuf buf)
    {
        NBTTagCompound tag = new NBTTagCompound();

        NetworkConfiguration.instance.getSharedConfig().writeToNBT(tag);

        ByteBufUtils.writeTag(buf, tag);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        received = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<MessageConfigResponse, IMessage>
    {

        @Override
        public IMessage onMessage(MessageConfigResponse message, MessageContext ctx)
        {
            if (message.received == null)
            {
                Core.LOG.severe("An error occurred while receiving server configuration.");
                return null;
            }
            // Messages arrive on the network thread; the configuration is applied on the client's.
            Minecraft.getMinecraft().addScheduledTask(() -> AnimationPolicy.INSTANCE.onServerConfiguration(message.received));
            return null;
        }

    }

}
