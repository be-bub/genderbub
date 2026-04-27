package com.bebub.genderbub.network;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.GenderMod;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GenderPacket {
    private final int entityId;
    private final boolean isOffhand;

    public GenderPacket(int entityId, boolean isOffhand) {
        this.entityId = entityId;
        this.isOffhand = isOffhand;
    }

    public static void encode(GenderPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.entityId);
        buf.writeBoolean(packet.isOffhand);
    }

    public static GenderPacket decode(FriendlyByteBuf buf) {
        return new GenderPacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(GenderPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            LivingEntity entity = (LivingEntity) player.level().getEntity(packet.entityId);
            if (entity == null) return;

            String gender = GenderCore.getGender(entity);
            if (gender.equals("none")) return;

            ItemStack stack = packet.isOffhand ? player.getOffhandItem() : player.getMainHandItem();
            if (stack.isEmpty()) return;

            ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (mobId == null) return;

            if (GenderConfig.isItemBlocked(mobId.toString(), gender, GenderCore.isSterile(entity), stack.getItem())) {
                return;
            }

            InteractionHand hand = packet.isOffhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            entity.interact(player, hand);
        });
        ctx.get().setPacketHandled(true);
    }
}