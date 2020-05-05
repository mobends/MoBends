package goblinbob.mobends.core.network.msg;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.network.SharedProperty;
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
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        if (tag == null)
        {
            Core.LOG.severe("An error occurred while receiving server configuration.");
            return;
        }

        NetworkConfiguration.instance.getSharedConfig().readFromNBT(tag);
    }

    public static class Handler implements IMessageHandler<MessageConfigResponse, IMessage>
    {

        @Override
        public IMessage onMessage(MessageConfigResponse message, MessageContext ctx)
        {
            final StringBuilder builder = new StringBuilder("Received Mo' Bends server configuration.\n");
            final Iterable<SharedProperty<?>> properties = NetworkConfiguration.instance.getSharedConfig().getProperties();
            for (SharedProperty<?> property : properties)
            {
                builder.append(String.format(" - %s: %b\n", property.getKey(), property.getValue()));
            }
            Core.LOG.info(builder.toString());

            return null;
        }

    }

}