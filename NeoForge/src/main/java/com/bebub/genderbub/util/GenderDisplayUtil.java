package com.bebub.genderbub.util;

import com.bebub.genderbub.GenderCore;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.ZombieVillager;

public class GenderDisplayUtil {
    
    private static final int MALE = 0x55AAFF;
    private static final int FEMALE = 0xFF55FF;
    private static final int STERILE = 0xAAAAAA;
    private static final int BABY = 0xFFAA55;
    private static final int CACHED = 0x888888;
    private static final int VILLAGER_MALE = 0x55AAFF;
    private static final int VILLAGER_FEMALE = 0xFF55FF;
    private static final int VILLAGER_STERILE = 0x88AA88;
    private static final int VILLAGER_ZOMBIE = 0x88FF88;
    private static final int VILLAGER_ZOMBIE_STERILE = 0x88AA88;
    
    public static int getColor(LivingEntity entity) {
        if (GenderCore.isGenderCached(entity)) {
            return CACHED;
        }
        
        String gender = GenderCore.getGender(entity);
        boolean sterile = GenderCore.isSterile(entity);
        boolean isBaby = entity instanceof AgeableMob && ((AgeableMob) entity).isBaby();
        boolean isVillager = entity instanceof Villager;
        boolean isZombieVillager = entity instanceof ZombieVillager;
        
        if (isZombieVillager) {
            return sterile ? VILLAGER_ZOMBIE_STERILE : VILLAGER_ZOMBIE;
        }
        
        if (isBaby || gender.equals("baby")) {
            return BABY;
        }
        
        if (sterile) {
            return STERILE;
        }
        
        if (isVillager) {
            return gender.equals("male") ? VILLAGER_MALE : VILLAGER_FEMALE;
        }
        
        return gender.equals("male") ? MALE : FEMALE;
    }
    
    public static String getTranslationKey(LivingEntity entity) {
        if (GenderCore.isGenderCached(entity)) {
            return "";
        }
        
        String gender = GenderCore.getGender(entity);
        boolean sterile = GenderCore.isSterile(entity);
        boolean isBaby = entity instanceof AgeableMob && ((AgeableMob) entity).isBaby();
        boolean isVillager = entity instanceof Villager;
        boolean isZombieVillager = entity instanceof ZombieVillager;
        
        if (isBaby || gender.equals("baby")) {
            return "genderbub.gender.baby";
        }
        
        if (isZombieVillager) {
            if (sterile) {
                return isBaby ? "genderbub.villager.sterile.baby" : "genderbub.villager.sterile." + gender;
            }
            return isBaby ? "genderbub.villager.baby" : "genderbub.villager." + gender;
        }
        
        if (isVillager) {
            if (sterile) {
                return isBaby ? "genderbub.villager.sterile.baby" : "genderbub.villager.sterile." + gender;
            }
            return isBaby ? "genderbub.villager.baby" : "genderbub.villager." + gender;
        }
        
        if (sterile) {
            return isBaby ? "genderbub.gender.sterile.baby" : "genderbub.gender.sterile." + gender;
        }
        return isBaby ? "genderbub.gender.baby" : "genderbub.gender." + gender;
    }
    
    public static Component getGenderComponent(LivingEntity entity) {
        if (GenderCore.isGenderCached(entity)) {
            return null;
        }
        
        String gender = GenderCore.getGender(entity);
        if (gender.equals("none")) return null;
        
        String key = getTranslationKey(entity);
        int color = getColor(entity);
        
        return Component.translatable(key).withStyle(Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(color)));
    }
}