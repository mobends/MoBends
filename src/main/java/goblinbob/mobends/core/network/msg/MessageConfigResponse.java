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
    private static final String ALLOW_BENDS_PACKS_KEY = "allowBendsPacks";
    private static final String MOVEMENT_LIMITED_KEY = "movementLimited";

    public boolean allowModelScaling;
    public boolean allowBendsPacks;
    public boolean movementLimited;

    /**
     * Necessary empty constructor, because of dynamic instancing.
     */
    public MessageConfigResponse() {}

    public MessageConfigResponse(boolean allowModelScaling, boolean allowBendsPacks, boolean movementLimited)
    {
        this.allowModelScaling = allowModelScaling;
        this.allowBendsPacks = allowBendsPacks;
        this.movementLimited = movementLimited;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        if (tag == null)
        {
            Core.LOG.severe("An error occurred while receiving server configuration.");
            this.allowModelScaling = false;
            this.allowBendsPacks = true;
            this.movementLimited = true;
            return;
        }
        this.allowModelScaling = tag.getBoolean(ALLOW_MODEL_SCALING_KEY);
        this.allowBendsPacks = tag.getBoolean(ALLOW_BENDS_PACKS_KEY);
        this.movementLimited = tag.getBoolean(MOVEMENT_LIMITED_KEY);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(ALLOW_MODEL_SCALING_KEY, this.allowModelScaling);
        tag.setBoolean(ALLOW_BENDS_PACKS_KEY, this.allowBendsPacks);
        tag.setBoolean(MOVEMENT_LIMITED_KEY, this.movementLimited);
        ByteBufUtils.writeTag(buf, tag);
    }

    public static class Handler implements IMessageHandler<MessageConfigResponse, IMessage>
    {

        @Override
        public IMessage onMessage(MessageConfigResponse message, MessageContext ctx)
        {
            Core.LOG.info("Received Mo' Bends server configuration.");
            Core.LOG.info(String.format(" - allowModelScaling: %b", message.allowModelScaling));
            Core.LOG.info(String.format(" - allowBendsPacks: %b", message.allowBendsPacks));
            Core.LOG.info(String.format(" - movementLimited: %b", message.movementLimited));

            NetworkConfiguration.instance.provideServerConfig(message);
            return null;
        }

    }

}