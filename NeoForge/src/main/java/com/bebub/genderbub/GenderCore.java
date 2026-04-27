package com.bebub.genderbub;

import com.bebub.genderbub.util.GenderDisplayUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class GenderCore {

    public static void init() {}
    public static void initClient() {}

    public static String getGender(LivingEntity entity) {
        return entity.getData(GenderMod.GENDER);
    }

    public static void setGender(LivingEntity entity, String gender) {
        entity.setData(GenderMod.GENDER, gender);
    }

    public static boolean isSterile(LivingEntity entity) {
        return entity.getData(GenderMod.STERILE);
    }

    public static void setSterile(LivingEntity entity, boolean sterile) {
        entity.setData(GenderMod.STERILE, sterile);
    }

    public static void clearGender(LivingEntity entity) {
        setGender(entity, "none");
        setSterile(entity, false);
    }

    public static boolean isGenderCached(LivingEntity entity) {
        return getGender(entity).startsWith("cached_");
    }

    public static void sendGenderMessage(ServerPlayer player, LivingEntity entity) {
        Component component = GenderDisplayUtil.getGenderComponent(entity);
        if (component != null) {
            player.displayClientMessage(component, true);
        }
    }
}