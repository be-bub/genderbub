package com.bebub.genderbub.network;

import com.bebub.genderbub.GenderMod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class GenderNetwork {
    public static void init(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(GenderMod.MOD_ID);
        registrar.playToServer(GenderPacket.TYPE, GenderPacket.STREAM_CODEC, GenderPacket::handle);
    }
}