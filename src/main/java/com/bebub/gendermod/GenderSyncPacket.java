package com.bebub.gendermod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import com.bebub.gendermod.client.ClientGenderCache;

import java.util.UUID;
import java.util.function.Supplier;

public class GenderSyncPacket {
    private final UUID animalId;
    private final String gender;

    public GenderSyncPacket(UUID animalId, String gender) {
        this.animalId = animalId;
        this.gender = gender;
    }

    public static void encode(GenderSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.animalId);
        buffer.writeUtf(packet.gender);
    }

    public static GenderSyncPacket decode(FriendlyByteBuf buffer) {
        return new GenderSyncPacket(buffer.readUUID(), buffer.readUtf());
    }

    public static void handle(GenderSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientGenderCache.put(packet.animalId, packet.gender);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}