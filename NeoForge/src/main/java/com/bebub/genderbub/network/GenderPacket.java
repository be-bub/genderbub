package com.bebub.genderbub.network;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.GenderMod;
import com.bebub.genderbub.config.GenderConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GenderPacket(int entityId, boolean isOffhand) implements CustomPacketPayload {
    public static final Type<GenderPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GenderMod.MOD_ID, "gender_packet"));
    public static final StreamCodec<ByteBuf, GenderPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, GenderPacket::entityId,
            ByteBufCodecs.BOOL, GenderPacket::isOffhand,
            GenderPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GenderPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null) return;
            LivingEntity entity = (LivingEntity) player.level().getEntity(packet.entityId);
            if (entity == null) return;
            String gender = GenderCore.getGender(entity);
            if (gender.equals("none")) return;
            ItemStack stack = packet.isOffhand() ? player.getOffhandItem() : player.getMainHandItem();
            if (stack.isEmpty()) return;
            ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (mobId == null) return;
            if (GenderConfig.isItemBlocked(mobId.toString(), gender, GenderCore.isSterile(entity), stack.getItem())) return;
            InteractionHand hand = packet.isOffhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            entity.interact(player, hand);
        });
    }
}