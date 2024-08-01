package net.shadowbeast.frostbound.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.shadowbeast.frostbound.temprature.util.EntityTempManager;

import java.util.function.Supplier;

public class TempModifiersSyncPacket
{
    int entityId;
    CompoundTag modifiers;

    public TempModifiersSyncPacket(LivingEntity entity, CompoundTag modifiers)
    {
        this.entityId = entity.getId();
        this.modifiers = modifiers;
    }

    TempModifiersSyncPacket(int entityId, CompoundTag modifiers)
    {
        this.entityId = entityId;
        this.modifiers = modifiers;
    }

    public static void encode(TempModifiersSyncPacket message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.modifiers);
    }

    public static TempModifiersSyncPacket decode(FriendlyByteBuf buffer)
    {
        return new TempModifiersSyncPacket(buffer.readInt(), buffer.readNbt());
    }

    public static void handle(TempModifiersSyncPacket message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient())
            context.enqueueWork(() ->
            {
                Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);

                if (entity instanceof LivingEntity living)
                {
                    EntityTempManager.getTemperatureCap(living).ifPresent(cap ->
                    {   cap.deserializeModifiers(message.modifiers);
                    });
                }
            });

        context.setPacketHandled(true);
    }
}
