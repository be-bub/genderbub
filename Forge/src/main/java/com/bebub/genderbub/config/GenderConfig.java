package com.bebub.genderbub.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.util.List;
import java.util.Set;

public class GenderConfig {
    
    public static void init() { 
        GenderLoader.init(); 
        GenderCache.loadFromData(GenderLoader.getData());
    }
    
    public static void reload() { 
        GenderLoader.load(); 
        GenderCache.loadFromData(GenderLoader.getData());
    }
    
    public static void reloadClientSettings() { 
        GenderLoader.load(); 
        GenderCache.loadFromData(GenderLoader.getData());
    }
    
    public static void resetToDefault() { 
        GenderLoader.reset(); 
        GenderCache.loadFromData(GenderLoader.getData());
    }
    
    public static void resetClientToDefault() {
        GenderLoader.getData().settings.displayRadius = 24;
        GenderLoader.getData().settings.hideWithJade = true;
        GenderLoader.getData().settings.requireScanner = true;
        GenderLoader.getData().settings.syncConfigRules = false;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
    }
    
    public static void save() { 
        GenderLoader.save(); 
    }
    
    public static void performFirstScanIfNeeded() { 
        GenderScanner.performFirstScan(GenderLoader.getData());
        GenderCache.loadFromData(GenderLoader.getData());
    }
    
    public static List<String> scanAndGetNewAnimals() { 
        List<String> newAnimals = GenderScanner.scanAndGetNew(GenderLoader.getData());
        GenderCache.loadFromData(GenderLoader.getData());
        return newAnimals;
    }
    
    public static int getMaleChance() { return GenderCache.getMaleChance(); }
    public static int getFemaleChance() { return GenderCache.getFemaleChance(); }
    public static int getDisplayRadius() { return GenderCache.getDisplayRadius(); }
    public static boolean isAllowMaleMaleBreed() { return GenderCache.isAllowMaleMaleBreed(); }
    public static boolean isAllowFemaleFemaleBreed() { return GenderCache.isAllowFemaleFemaleBreed(); }
    public static boolean isAllowSterileBreed() { return GenderCache.isAllowSterileBreed(); }
    public static boolean isEnableVillagers() { return GenderCache.isEnableVillagers(); }
    public static boolean isKeepVillagerGender() { return GenderCache.isKeepVillagerGender(); }
    public static boolean isHideWithJade() { return GenderCache.isHideWithJade(); }
    public static boolean isRequireScanner() { return GenderCache.isRequireScanner(); }
    public static boolean isSyncConfigRules() { return GenderCache.isSyncConfigRules(); }
    
    public static Set<String> getEnabledMobs() { return GenderCache.getEnabledMobs(); }
    public static Set<String> getGenderOnlyMobs() { return GenderCache.getGenderOnlyMobs(); }
    
    public static boolean isMobEnabled(String mobId) {
        return GenderCache.isMobEnabled(mobId);
    }
    
    public static boolean isScannerItem(Item item) {
        if (item == null) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id != null && id.toString().equals("genderbub:magnifying_glass");
    }
    
    public static boolean isItemBlocked(String mobId, String gender, boolean sterile, Item item) {
        GenderData.InteractionRule rule = GenderCache.getInteraction(mobId);
        if (rule == null) return false;
        
        ResourceLocation itemIdLoc = BuiltInRegistries.ITEM.getKey(item);
        if (itemIdLoc == null) return false;
        String itemIdStr = itemIdLoc.toString();
        
        return rule.isGenderMatch(gender, sterile) && rule.isItemMatch(itemIdStr);
    }
    
    public static boolean isEggLayingBlocked(String mobId, String gender, boolean sterile) {
        GenderData.EggRule rule = GenderCache.getEggRule(mobId);
        if (rule == null) return false;
        return rule.isGenderMatch(gender, sterile);
    }
    
    public static String[] getRandomGenderWithSterile() {
        return GenderMatcher.getRandomGenderWithSterile(getMaleChance(), getFemaleChance());
    }
    
    public static java.nio.file.Path getConfigPath() { return GenderLoader.getConfigPath(); }

    public static int getInteractionsCount() {
        return GenderLoader.getData().interactions.size();
    }
    
    public static int getEggRulesCount() {
        return GenderLoader.getData().eggRules.size();
    }
    
    public static String getConfigHash() {
        try {
            java.nio.file.Path path = getConfigPath();
            if (!java.nio.file.Files.exists(path)) return "";
            byte[] bytes = java.nio.file.Files.readAllBytes(path);
            return Integer.toHexString(java.util.Arrays.hashCode(bytes));
        } catch (Exception e) {
            return "";
        }
    }
}