package com.bebub.genderbub.compat;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.GenderMod;
import com.bebub.genderbub.config.GenderLoader;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GenderAddon {
    
    private static final Map<String, GenderLoader.MobCompatRule> RULES = new HashMap<>();
    private static final Random RANDOM = new Random();
    
    private static boolean shouldBeSterile(int chance) {
        if (chance <= 0) return false;
        if (chance >= 100) return true;
        return RANDOM.nextInt(100) < chance;
    }
    
    public static void loadRules(List<GenderLoader.MobCompatRule> rules) {
        RULES.clear();
        for (GenderLoader.MobCompatRule rule : rules) {
            if (rule.mobId != null && (rule.rules != null || rule.forceGender != null)) {
                RULES.put(rule.mobId, rule);
                GenderMod.LOGGER.info("Loaded compat rule for: {}", rule.mobId);
            }
        }
        GenderMod.LOGGER.info("Total compat rules loaded: {}", RULES.size());
    }
    
    public static void apply(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return;
        
        GenderLoader.MobCompatRule rule = RULES.get(id.toString());
        if (rule == null) return;
        
        boolean isBaby = entity instanceof AgeableMob && ((AgeableMob) entity).isBaby();
        String currentGender = GenderCore.getGender(entity);
        boolean isFirstTime = currentGender.equals("none") || currentGender.startsWith("cached_");
        
        if (isBaby && rule.rules != null) {
            for (GenderLoader.CompatRule r : rule.rules) {
                if ("baby".equals(r.gender)) {
                    Boolean result = isBaby;
                    if (result != null && result == r.expected) {
                        GenderCore.setGender(entity, "baby");
                        if (isFirstTime) {
                            applySterile(entity, rule);
                        }
                        return;
                    }
                }
            }
        }
        
        if (rule.forceGender != null && !rule.forceGender.isEmpty()) {
            GenderCore.setGender(entity, rule.forceGender);
            if (isFirstTime) {
                applySterile(entity, rule);
            }
            return;
        }
        
        if (rule.rules != null) {
            for (GenderLoader.CompatRule r : rule.rules) {
                Boolean result = callMethod(entity, r.method);
                if (result != null && result == r.expected) {
                    GenderCore.setGender(entity, r.gender);
                    if (isFirstTime) {
                        applySterile(entity, rule);
                    }
                    return;
                }
            }
        }
    }
    
    private static void applySterile(LivingEntity entity, GenderLoader.MobCompatRule rule) {
        if (GenderCore.isSterile(entity)) {
            return;
        }
        
        if (rule.sterileMethod != null && !rule.sterileMethod.isEmpty()) {
            Boolean result = callMethod(entity, rule.sterileMethod);
            if (result != null) {
                boolean sterile = rule.sterileInvert ? !result : result;
                GenderCore.setSterile(entity, sterile);
                return;
            }
        }
        
        if (rule.sterileChance >= 0) {
            GenderCore.setSterile(entity, shouldBeSterile(rule.sterileChance));
            return;
        }
        
        if (rule.useGlobalSterile != null && rule.useGlobalSterile) {
            String currentGender = GenderCore.getGender(entity);
            if (currentGender.equals("male")) {
                int sterileChance = 50 - GenderConfig.getMaleChance();
                if (GenderConfig.getMaleChance() == 0) {
                    sterileChance = 100;
                }
                GenderCore.setSterile(entity, shouldBeSterile(sterileChance));
            } else if (currentGender.equals("female")) {
                int sterileChance = 50 - GenderConfig.getFemaleChance();
                if (GenderConfig.getFemaleChance() == 0) {
                    sterileChance = 100;
                }
                GenderCore.setSterile(entity, shouldBeSterile(sterileChance));
            } else if (currentGender.equals("baby")) {
                if (RANDOM.nextBoolean()) {
                    int sterileChance = 50 - GenderConfig.getMaleChance();
                    if (GenderConfig.getMaleChance() == 0) {
                        sterileChance = 100;
                    }
                    GenderCore.setSterile(entity, shouldBeSterile(sterileChance));
                } else {
                    int sterileChance = 50 - GenderConfig.getFemaleChance();
                    if (GenderConfig.getFemaleChance() == 0) {
                        sterileChance = 100;
                    }
                    GenderCore.setSterile(entity, shouldBeSterile(sterileChance));
                }
            }
        }
    }
    
    public static void applyForce(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return;
        
        GenderLoader.MobCompatRule rule = RULES.get(id.toString());
        if (rule == null) return;
        
        if (rule.forceGender != null && !rule.forceGender.isEmpty()) {
            GenderCore.setGender(entity, rule.forceGender);
            applySterile(entity, rule);
        }
    }
    
    public static GenderLoader.MobCompatRule getRule(LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return null;
        return RULES.get(id.toString());
    }
    
    private static Boolean callMethod(LivingEntity entity, String methodName) {
        try {
            Method method = null;
            Class<?> clazz = entity.getClass();
            while (clazz != null && method == null) {
                try {
                    method = clazz.getDeclaredMethod(methodName);
                } catch (NoSuchMethodException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if (method == null) return null;
            method.setAccessible(true);
            return (Boolean) method.invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }
}
