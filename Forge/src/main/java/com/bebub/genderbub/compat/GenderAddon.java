package com.bebub.genderbub.compat;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;

import java.lang.reflect.Method;
import java.util.Random;

public class GenderAddon {
    
    private static final Random RANDOM = new Random();
    
    private static boolean shouldBeSterile(int maleChance, int femaleChance, String gender) {
        int chance = gender.equals("male") ? maleChance : femaleChance;
        if (chance <= 0) return false;
        int sterileChance = 50 - chance;
        if (sterileChance <= 0) return false;
        return RANDOM.nextInt(50) >= chance;
    }
    
    private static final String NATURALIST_LION_CLASS = "com.starfish_studios.naturalist.common.entity.Lion";
    private static Method naturalistHasManeMethod = null;
    private static boolean naturalistReflectionFailed = false;
    
    public static boolean isNaturalistLion(LivingEntity e) {
        return e.getClass().getName().equals(NATURALIST_LION_CLASS);
    }
    
    public static boolean isNaturalistLionBaby(LivingEntity e) {
        return e instanceof AgeableMob && ((AgeableMob) e).isBaby();
    }
    
    private static String getNaturalistLionGender(LivingEntity e) {
        if (naturalistReflectionFailed) return null;
        
        try {
            if (naturalistHasManeMethod == null) {
                naturalistHasManeMethod = e.getClass().getMethod("hasMane");
            }
            boolean hasMane = (boolean) naturalistHasManeMethod.invoke(e);
            return hasMane ? "male" : "female";
        } catch (Exception ex) {
            naturalistReflectionFailed = true;
        }
        return null;
    }
    
    private static final String PRIMAL_LION_CLASS = "org.primal.entity.animal.LionEntity";
    private static Method primalIsManelessMethod = null;
    private static boolean primalReflectionFailed = false;
    
    public static boolean isPrimalLion(LivingEntity e) {
        return e.getClass().getName().equals(PRIMAL_LION_CLASS);
    }
    
    public static boolean isPrimalLionBaby(LivingEntity e) {
        return e instanceof AgeableMob && ((AgeableMob) e).isBaby();
    }
    
    private static String getPrimalLionGender(LivingEntity e) {
        if (primalReflectionFailed) return null;
        
        try {
            if (primalIsManelessMethod == null) {
                primalIsManelessMethod = e.getClass().getMethod("isManeless");
            }
            boolean isManeless = (boolean) primalIsManelessMethod.invoke(e);
            return isManeless ? "female" : "male";
        } catch (Exception ex) {
            primalReflectionFailed = true;
        }
        return null;
    }
    
    private static final String ENVIRONMENTAL_DEER_CLASS = "com.teamabnormals.environmental.common.entity.animal.deer.AbstractDeer";
    private static Method hasAntlersMethod = null;
    private static boolean environmentalReflectionFailed = false;
    
    public static boolean isEnvironmentalDeer(LivingEntity e) {
        Class<?> clazz = e.getClass();
        while (clazz != null) {
            if (clazz.getName().equals(ENVIRONMENTAL_DEER_CLASS)) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }
    
    public static boolean isEnvironmentalDeerBaby(LivingEntity e) {
        return e instanceof AgeableMob && ((AgeableMob) e).isBaby();
    }
    
    private static String getEnvironmentalDeerGender(LivingEntity e) {
        if (environmentalReflectionFailed) return null;
        
        try {
            if (hasAntlersMethod == null) {
                hasAntlersMethod = e.getClass().getMethod("hasAntlers");
            }
            boolean hasAntlers = (boolean) hasAntlersMethod.invoke(e);
            return hasAntlers ? "male" : "female";
        } catch (Exception ex) {
            environmentalReflectionFailed = true;
        }
        return null;
    }
    
    private static Method iceFireIsMaleMethod = null;
    private static boolean iceFireReflectionFailed = false;
    
    public static boolean isIceFireDragon(LivingEntity e) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        return id != null && id.getNamespace().equals("iceandfire") && id.getPath().contains("dragon");
    }
    
    public static String getIceFireDragonGender(LivingEntity e) {
        if (iceFireReflectionFailed) return null;
        
        try {
            if (iceFireIsMaleMethod == null) {
                iceFireIsMaleMethod = e.getClass().getMethod("isMale");
            }
            boolean isMale = (boolean) iceFireIsMaleMethod.invoke(e);
            return isMale ? "male" : "female";
        } catch (Exception ex) {
            iceFireReflectionFailed = true;
        }
        return null;
    }
    
    public static boolean canIceFireDragonMate(Animal self, Animal other) {
        String g1 = getIceFireDragonGender(self);
        String g2 = getIceFireDragonGender(other);
        if (g1 == null || g2 == null) return true;
        if (g1.equals(g2)) {
            if ((g1.equals("male") && !GenderConfig.isAllowMaleMaleBreed()) ||
                (g1.equals("female") && !GenderConfig.isAllowFemaleFemaleBreed())) return false;
        }
        return true;
    }
    
    public static boolean isExternalMob(LivingEntity e) {
        return isNaturalistLion(e) || isPrimalLion(e) || isIceFireDragon(e) || isEnvironmentalDeer(e);
    }
    
    public static String getExternalMobId(LivingEntity e) {
        if (isNaturalistLion(e)) return "naturalist:lion";
        if (isPrimalLion(e)) return "primal:lion";
        if (isEnvironmentalDeer(e)) return "environmental:deer";
        if (isIceFireDragon(e)) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
            return id != null ? id.toString() : null;
        }
        return null;
    }
    
    public static void assignGenderIfMissing(LivingEntity e) {
        if (isNaturalistLion(e)) {
            boolean isBaby = isNaturalistLionBaby(e);
            String current = GenderCore.getGender(e);
            
            if (isBaby) {
                if (!current.equals("baby")) {
                    GenderCore.setGender(e, "baby");
                    GenderCore.setSterile(e, false);
                }
                return;
            }
            
            if (current.equals("baby") || current.equals("none")) {
                String gender = getNaturalistLionGender(e);
                if (gender != null) {
                    boolean sterile = shouldBeSterile(GenderConfig.getMaleChance(), GenderConfig.getFemaleChance(), gender);
                    GenderCore.setGender(e, gender);
                    GenderCore.setSterile(e, sterile);
                }
            }
            return;
        }
        
        if (isPrimalLion(e)) {
            boolean isBaby = isPrimalLionBaby(e);
            String current = GenderCore.getGender(e);
            
            if (isBaby) {
                if (!current.equals("baby")) {
                    GenderCore.setGender(e, "baby");
                    GenderCore.setSterile(e, false);
                }
                return;
            }
            
            if (current.equals("baby") || current.equals("none")) {
                String gender = getPrimalLionGender(e);
                if (gender != null) {
                    boolean sterile = shouldBeSterile(GenderConfig.getMaleChance(), GenderConfig.getFemaleChance(), gender);
                    GenderCore.setGender(e, gender);
                    GenderCore.setSterile(e, sterile);
                }
            }
            return;
        }
        
        if (isEnvironmentalDeer(e)) {
            boolean isBaby = isEnvironmentalDeerBaby(e);
            String current = GenderCore.getGender(e);
            
            if (isBaby) {
                if (!current.equals("baby")) {
                    GenderCore.setGender(e, "baby");
                    GenderCore.setSterile(e, false);
                }
                return;
            }
            
            if (current.equals("baby") || current.equals("none")) {
                String gender = getEnvironmentalDeerGender(e);
                if (gender != null) {
                    boolean sterile = shouldBeSterile(GenderConfig.getMaleChance(), GenderConfig.getFemaleChance(), gender);
                    GenderCore.setGender(e, gender);
                    GenderCore.setSterile(e, sterile);
                }
            }
            return;
        }
        
        if (isIceFireDragon(e)) {
            String current = GenderCore.getGender(e);
            if (current.equals("none")) {
                String gender = getIceFireDragonGender(e);
                if (gender != null) {
                    GenderCore.setGender(e, gender);
                    GenderCore.setSterile(e, false);
                }
            }
        }
    }
}