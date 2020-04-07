package goblinbob.mobends.core.network.msg;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * This message is sent by the client to the server to request
 * the server specific configuration.
 */
public class MessageConfigRequest implements IMessage
{

    /**
     * Necessary empty constructor, because of dynamic instancing.
     */
    public MessageConfigRequest() {}

    @Override
    public void fromBytes(ByteBuf buf)
    {
        // No data transferred.
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        // No data transferred.
    }

    public static class Handler implements IMessageHandler<MessageConfigRequest, MessageConfigResponse>
    {

        @Override
        public MessageConfigResponse onMessage(MessageConfigRequest message, MessageContext ctx)
        {
            return new MessageConfigResponse();
        }

    }

}
