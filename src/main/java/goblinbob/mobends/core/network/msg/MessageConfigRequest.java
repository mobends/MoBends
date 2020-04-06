package goblinbob.mobends.core.network.msg;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.CoreServer;
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
            if (CoreServer.getInstance() == null)
            {
                Core.LOG.severe("The CORE isn't a server core, something is wrong...");
                return null;
            }

            final CoreServer core = CoreServer.getInstance();
            return new MessageConfigResponse(core.getConfiguration().isModelScalingAllowed());
        }

    }

}
