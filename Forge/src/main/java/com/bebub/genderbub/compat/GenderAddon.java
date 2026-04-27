package com.bebub.genderbub.compat;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.GenderMod;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;

import java.lang.reflect.Method;

public class GenderAddon {
    
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
            GenderMod.LOGGER.debug("Failed to get Naturalist lion gender via reflection: {}", ex.getMessage());
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
            GenderMod.LOGGER.debug("Failed to get Primal lion gender via reflection: {}", ex.getMessage());
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
            GenderMod.LOGGER.debug("Failed to get IceAndFire dragon gender via reflection: {}", ex.getMessage());
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
        return isNaturalistLion(e) || isPrimalLion(e) || isIceFireDragon(e);
    }
    
    public static String getExternalMobId(LivingEntity e) {
        if (isNaturalistLion(e)) return "naturalist:lion";
        if (isPrimalLion(e)) return "primal:lion";
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
            
            if (current.equals("baby")) {
                String gender = getNaturalistLionGender(e);
                if (gender != null) {
                    GenderCore.setGender(e, gender);
                    GenderCore.setSterile(e, false);
                }
            } else if (current.equals("none")) {
                String gender = getNaturalistLionGender(e);
                if (gender != null) {
                    GenderCore.setGender(e, gender);
                    GenderCore.setSterile(e, false);
                }
            }
        } else if (isPrimalLion(e)) {
            boolean isBaby = isPrimalLionBaby(e);
            String current = GenderCore.getGender(e);
            
            if (isBaby) {
                if (!current.equals("baby")) {
                    GenderCore.setGender(e, "baby");
                    GenderCore.setSterile(e, false);
                }
                return;
            }
            
            if (current.equals("baby")) {
                String gender = getPrimalLionGender(e);
                if (gender != null) {
                    GenderCore.setGender(e, gender);
                    GenderCore.setSterile(e, false);
                }
            } else if (current.equals("none")) {
                String gender = getPrimalLionGender(e);
                if (gender != null) {
                    GenderCore.setGender(e, gender);
                    GenderCore.setSterile(e, false);
                }
            }
        } else if (isIceFireDragon(e)) {
            String current = GenderCore.getGender(e);
            if (!current.equals("none")) return;
            String gender = getIceFireDragonGender(e);
            if (gender != null) {
                GenderCore.setGender(e, gender);
                GenderCore.setSterile(e, false);
            }
        }
    }
}