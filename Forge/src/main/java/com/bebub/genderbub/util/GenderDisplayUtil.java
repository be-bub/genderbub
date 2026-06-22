package com.bebub.genderbub.util;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.compat.GenderAddon;
import com.bebub.genderbub.config.GenderLoader;
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
    private static final int BABY_DEFAULT = 0xFFFFFF;
    private static final int CACHED = 0x888888;
    private static final int VILLAGER_MALE = 0x55AAFF;
    private static final int VILLAGER_FEMALE = 0xFF55FF;
    private static final int VILLAGER_STERILE = 0x88AA88;
    private static final int VILLAGER_ZOMBIE = 0x88FF88;
    private static final int VILLAGER_ZOMBIE_STERILE = 0x88AA88;
    
    private static int parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) return -1;
        try {
            if (colorStr.startsWith("0x") || colorStr.startsWith("0X")) {
                return Integer.parseInt(colorStr.substring(2), 16);
            }
            return Integer.parseInt(colorStr, 16);
        } catch (Exception e) {
            return -1;
        }
    }
    
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
            GenderLoader.MobCompatRule rule = GenderAddon.getRule(entity);
            if (rule != null && rule.colors != null && rule.colors.baby != null) {
                int color = parseColor(rule.colors.baby);
                if (color != -1) {
                    return color;
                }
            }
            if (gender.equals("baby")) {
                return BABY_DEFAULT;
            }
            return gender.equals("male") ? MALE : FEMALE;
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
        
        if (isVillager && isBaby) {
            return "genderbub.villager.baby";
        }
        
        if (isVillager) {
            if (sterile) {
                return "genderbub.villager.sterile." + gender;
            }
            return "genderbub.villager." + gender;
        }
        
        if (isZombieVillager) {
            if (sterile) {
                return "genderbub.villager.sterile." + gender;
            }
            return "genderbub.villager." + gender;
        }
        
        if (isBaby || gender.equals("baby")) {
            return "genderbub.gender.baby";
        }
        
        if (sterile) {
            return "genderbub.gender.sterile." + gender;
        }
        return "genderbub.gender." + gender;
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