package net.shadowbeast.arcanemysteries.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.shadowbeast.arcanemysteries.networking.ModMessages;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientConfigAskMessage
{
    UUID openerUUID;

    public ClientConfigAskMessage(UUID openerUUID)
    {
        this.openerUUID = openerUUID;
    }

    public ClientConfigAskMessage()
    {
        this(SyncConfigSettingsMessage.EMPTY_UUID);
    }

    public static void encode(ClientConfigAskMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.openerUUID);
    }

    public static ClientConfigAskMessage decode(FriendlyByteBuf buffer)
    {
        return new ClientConfigAskMessage(buffer.readUUID());
    }

    public static void handle(ClientConfigAskMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isServer()
                    && context.getSender() != null)
            {
                ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(context::getSender), new SyncConfigSettingsMessage(message.openerUUID));
            }
        });
        context.setPacketHandled(true);
    }
}