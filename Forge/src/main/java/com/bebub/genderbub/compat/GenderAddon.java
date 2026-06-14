package com.bebub.genderbub.compat;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.GenderMod;
import com.bebub.genderbub.config.GenderLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

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
            }
        }
    }
    
    public static void apply(LivingEntity entity) {
        var id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id == null) return;
        
        var rule = RULES.get(id.toString());
        if (rule == null) return;
        
        if (rule.forceGender != null && !rule.forceGender.isEmpty()) {
            GenderCore.setGender(entity, rule.forceGender);
            applySterile(entity, rule);
            return;
        }
        
        if (rule.rules != null) {
            for (var r : rule.rules) {
                Boolean result = callMethod(entity, r.method);
                if (result != null && result == r.expected) {
                    GenderCore.setGender(entity, r.gender);
                    applySterile(entity, rule);
                    return;
                }
            }
        }
    }
    
    private static void applySterile(LivingEntity entity, GenderLoader.MobCompatRule rule) {
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
        }
    }
    
    public static void applyForce(LivingEntity entity) {
        var id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id == null) return;
        
        var rule = RULES.get(id.toString());
        if (rule == null) return;
        
        if (rule.forceGender != null && !rule.forceGender.isEmpty()) {
            GenderCore.setGender(entity, rule.forceGender);
            applySterile(entity, rule);
        }
    }
    
    public static GenderLoader.MobCompatRule getRule(LivingEntity entity) {
        var id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id == null) return null;
        return RULES.get(id.toString());
    }
    
    private static Boolean callMethod(LivingEntity entity, String methodName) {
        try {
            Method method = entity.getClass().getMethod(methodName);
            return (Boolean) method.invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }
}