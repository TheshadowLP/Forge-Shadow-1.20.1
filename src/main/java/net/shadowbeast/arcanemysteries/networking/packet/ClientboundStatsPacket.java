package net.shadowbeast.arcanemysteries.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.shadowbeast.arcanemysteries.networking.MessagesMod;
import net.shadowbeast.arcanemysteries.temprature.util.EStats;

public class ClientboundStatsPacket extends ClientboundUnionPacket {
    private CompoundTag stats;

    public ClientboundStatsPacket(final CompoundTag statsIn) {
        super(MessagesMod.INSTANCE);
        this.stats = statsIn;
    }

    public ClientboundStatsPacket(final ServerPlayer player){
        this(EStats.getModNBT(player));
    }

    public ClientboundStatsPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf, MessagesMod.INSTANCE);
        this.stats = byteBuf.readNbt();
    }

    @Override
    public void encode(final FriendlyByteBuf byteBuf) {
        byteBuf.writeNbt(this.stats);
    }

    @Override
    public boolean handleOnClient(LocalPlayer sender) {
        assert Minecraft.getInstance().player != null;
        EStats.setModNBT(this.stats, Minecraft.getInstance().player);
        return true;
    }
}
