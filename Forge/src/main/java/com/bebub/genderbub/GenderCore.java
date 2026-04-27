package com.bebub.genderbub;

import com.bebub.genderbub.api.GenderHolder;
import com.bebub.genderbub.util.GenderDisplayUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class GenderCore {
    
    public static void init() {}
    
    public static void initClient() {}
    
    public static String getGender(LivingEntity entity) {
        return entity instanceof GenderHolder holder ? holder.getGender() : "none";
    }
    
    public static void setGender(LivingEntity entity, String gender) {
        if (entity instanceof GenderHolder holder) holder.setGender(gender);
    }
    
    public static boolean isSterile(LivingEntity entity) {
        return entity instanceof GenderHolder holder && holder.isSterile();
    }
    
    public static void setSterile(LivingEntity entity, boolean sterile) {
        if (entity instanceof GenderHolder holder) holder.setSterile(sterile);
    }
    
    public static void clearGender(LivingEntity entity) {
        setGender(entity, "none");
        setSterile(entity, false);
    }
    
    public static boolean isGenderCached(LivingEntity entity) {
        String gender = getGender(entity);
        return gender.startsWith("cached_");
    }
    
    public static void sendGenderMessage(ServerPlayer player, LivingEntity entity) {
        Component component = GenderDisplayUtil.getGenderComponent(entity);
        if (component != null) {
            player.displayClientMessage(component, true);
        }
    }
}