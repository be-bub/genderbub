package com.bebub.genderbub.util;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.compat.GenderAddon;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.ZombieVillager;

public class GenderDisplayUtil {
    
    public static int getColor(LivingEntity entity) {
        if (GenderCore.isGenderCached(entity)) {
            return 0x888888;
        }
        
        String gender = GenderCore.getGender(entity);
        boolean sterile = GenderCore.isSterile(entity);
        boolean isBaby = entity instanceof AgeableMob && ((AgeableMob) entity).isBaby();
        boolean isVillager = entity instanceof Villager;
        boolean isZombieVillager = entity instanceof ZombieVillager;
        boolean isDragon = GenderAddon.isIceFireDragon(entity);
        boolean isNaturalistLionBaby = GenderAddon.isNaturalistLion(entity) && isBaby;
        boolean isPrimalLionBaby = GenderAddon.isPrimalLion(entity) && isBaby;
        
        if (isZombieVillager) {
            return sterile ? 0x88AA88 : 0x88FF88;
        }
        
        if (gender.equals("baby") || isNaturalistLionBaby || isPrimalLionBaby) {
            return 0xFFAA55;
        }
        
        if (sterile) return 0xAAAAAA;
        
        if (isDragon) {
            return gender.equals("male") ? 0x55AAFF : 0xFF55FF;
        }
        
        return gender.equals("male") ? 0x55AAFF : 0xFF55FF;
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
        boolean isDragon = GenderAddon.isIceFireDragon(entity);
        boolean isNaturalistLionBaby = GenderAddon.isNaturalistLion(entity) && isBaby;
        boolean isPrimalLionBaby = GenderAddon.isPrimalLion(entity) && isBaby;
        
        if (gender.equals("baby") || isNaturalistLionBaby || isPrimalLionBaby) {
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
        
        if (isDragon) {
            if (sterile) {
                return gender.equals("male") ? "genderbub.gender.sterile.male" : "genderbub.gender.sterile.female";
            }
            return gender.equals("male") ? "genderbub.gender.male" : "genderbub.gender.female";
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
        
        if (gender.equals("baby")) {
            return Component.translatable(key).withStyle(Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(color)));
        }
        
        String icon = gender.equals("male") ? "♂ " : "♀ ";
        return Component.literal(icon).append(Component.translatable(key)).withStyle(Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(color)));
    }
}