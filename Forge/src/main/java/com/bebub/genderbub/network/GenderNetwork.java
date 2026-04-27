package com.bebub.genderbub.network;

import com.bebub.genderbub.GenderMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class GenderNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(GenderMod.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void init() {
        CHANNEL.registerMessage(0, GenderPacket.class, GenderPacket::encode, GenderPacket::decode, GenderPacket::handle);
    }

    public static void sendInteraction(int entityId, boolean isOffhand) {
        CHANNEL.sendToServer(new GenderPacket(entityId, isOffhand));
    }
}