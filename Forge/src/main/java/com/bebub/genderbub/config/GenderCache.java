package com.bebub.genderbub.config;

import com.bebub.genderbub.GenderMod;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GenderCache {
    
    private static final Set<String> ENABLED_MOBS = ConcurrentHashMap.newKeySet();
    private static final Set<String> GENDER_ONLY_MOBS = ConcurrentHashMap.newKeySet();
    private static final Map<String, GenderData.InteractionRule> INTERACTIONS = new ConcurrentHashMap<>();
    private static final Map<String, GenderData.EggRule> EGG_RULES = new ConcurrentHashMap<>();
    
    private static int maleChance;
    private static int femaleChance;
    private static int displayRadius;
    private static boolean hideWithJade;
    private static boolean requireScanner;
    private static boolean syncConfigRules;
    private static boolean allowMaleMaleBreed;
    private static boolean allowFemaleFemaleBreed;
    private static boolean allowSterileBreed;
    private static boolean enableVillagers;
    private static boolean keepVillagerGender;
    
    public static void loadFromData(GenderData data) {
        GenderMod.LOGGER.info("Loading cache from data...");
        
        ENABLED_MOBS.clear();
        GENDER_ONLY_MOBS.clear();
        INTERACTIONS.clear();
        EGG_RULES.clear();
        
        ENABLED_MOBS.addAll(data.settings.enabledMobs);
        GENDER_ONLY_MOBS.addAll(data.settings.genderOnlyMobs);
        
        for (GenderData.InteractionRule rule : data.interactions) {
            INTERACTIONS.put(rule.mobId, rule);
        }
        
        for (GenderData.EggRule rule : data.eggRules) {
            EGG_RULES.put(rule.mobId, rule);
        }
        
        maleChance = data.settings.maleChance;
        femaleChance = data.settings.femaleChance;
        displayRadius = data.settings.displayRadius;
        hideWithJade = data.settings.hideWithJade;
        requireScanner = data.settings.requireScanner;
        syncConfigRules = data.settings.syncConfigRules;
        allowMaleMaleBreed = data.settings.allowMaleMaleBreed;
        allowFemaleFemaleBreed = data.settings.allowFemaleFemaleBreed;
        allowSterileBreed = data.settings.allowSterileBreed;
        enableVillagers = data.settings.enableVillagers;
        keepVillagerGender = data.settings.keepVillagerGender;
        
        GenderMod.LOGGER.info("Cache loaded: {} enabled mobs, {} gender only mobs, {} interactions, {} egg rules",
            ENABLED_MOBS.size(), GENDER_ONLY_MOBS.size(), INTERACTIONS.size(), EGG_RULES.size());
    }
    
    public static void clear() {
        ENABLED_MOBS.clear();
        GENDER_ONLY_MOBS.clear();
        INTERACTIONS.clear();
        EGG_RULES.clear();
    }
    
    public static boolean isMobEnabled(String mobId) {
        if (mobId == null) return false;
        if (mobId.equals("minecraft:villager") || mobId.equals("minecraft:zombie_villager")) {
            return enableVillagers;
        }
        return ENABLED_MOBS.contains(mobId);
    }
    
    public static boolean isGenderOnlyMob(String mobId) {
        if (mobId == null) return false;
        return GENDER_ONLY_MOBS.contains(mobId);
    }
    
    public static GenderData.InteractionRule getInteraction(String mobId) {
        if (mobId == null) return null;
        return INTERACTIONS.get(mobId);
    }
    
    public static GenderData.EggRule getEggRule(String mobId) {
        if (mobId == null) return null;
        return EGG_RULES.get(mobId);
    }
    
    public static Set<String> getEnabledMobs() {
        return ENABLED_MOBS;
    }
    
    public static Set<String> getGenderOnlyMobs() {
        return GENDER_ONLY_MOBS;
    }
    
    public static int getMaleChance() { return maleChance; }
    public static int getFemaleChance() { return femaleChance; }
    public static int getDisplayRadius() { return displayRadius; }
    public static boolean isHideWithJade() { return hideWithJade; }
    public static boolean isRequireScanner() { return requireScanner; }
    public static boolean isSyncConfigRules() { return syncConfigRules; }
    public static boolean isAllowMaleMaleBreed() { return allowMaleMaleBreed; }
    public static boolean isAllowFemaleFemaleBreed() { return allowFemaleFemaleBreed; }
    public static boolean isAllowSterileBreed() { return allowSterileBreed; }
    public static boolean isEnableVillagers() { return enableVillagers; }
    public static boolean isKeepVillagerGender() { return keepVillagerGender; }
    
    public static void setMaleChance(int value) { maleChance = value; }
    public static void setFemaleChance(int value) { femaleChance = value; }
    public static void setDisplayRadius(int value) { displayRadius = value; }
    public static void setHideWithJade(boolean value) { hideWithJade = value; }
    public static void setRequireScanner(boolean value) { requireScanner = value; }
    public static void setSyncConfigRules(boolean value) { syncConfigRules = value; }
    public static void setAllowMaleMaleBreed(boolean value) { allowMaleMaleBreed = value; }
    public static void setAllowFemaleFemaleBreed(boolean value) { allowFemaleFemaleBreed = value; }
    public static void setAllowSterileBreed(boolean value) { allowSterileBreed = value; }
    public static void setEnableVillagers(boolean value) { enableVillagers = value; }
    public static void setKeepVillagerGender(boolean value) { keepVillagerGender = value; }
}