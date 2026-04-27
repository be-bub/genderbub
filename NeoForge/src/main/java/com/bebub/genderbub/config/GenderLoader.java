package com.bebub.genderbub.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.bebub.genderbub.GenderMod;
import java.io.*;
import java.nio.file.Path;

public class GenderLoader {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "genderbub", "genderbub.json");
    private static final Path BACKUP_DIR = Path.of("config", "genderbub", "backups");
    private static final int MAX_BACKUPS = 10;
    private static GenderData data;
    
    public static void init() {
        CONFIG_PATH.getParent().toFile().mkdirs();
        BACKUP_DIR.toFile().mkdirs();
        load();
    }
    
    public static void load() {
        if (!CONFIG_PATH.toFile().exists()) {
            createDefault();
            return;
        }
        
        try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            
            GenderData defaultData = new GenderData();
            
            if (root.has("settings")) {
                JsonObject settings = root.getAsJsonObject("settings");
                
                settings.addProperty("maleChance", getIntOrDefault(settings, "maleChance", defaultData.settings.maleChance));
                settings.addProperty("femaleChance", getIntOrDefault(settings, "femaleChance", defaultData.settings.femaleChance));
                settings.addProperty("displayRadius", getIntWithBounds(settings, "displayRadius", defaultData.settings.displayRadius, 0, 128));
                settings.addProperty("hideWithJade", getBooleanOrDefault(settings, "hideWithJade", defaultData.settings.hideWithJade));
                settings.addProperty("allowMaleMaleBreed", getBooleanOrDefault(settings, "allowMaleMaleBreed", defaultData.settings.allowMaleMaleBreed));
                settings.addProperty("allowFemaleFemaleBreed", getBooleanOrDefault(settings, "allowFemaleFemaleBreed", defaultData.settings.allowFemaleFemaleBreed));
                settings.addProperty("allowSterileBreed", getBooleanOrDefault(settings, "allowSterileBreed", defaultData.settings.allowSterileBreed));
                settings.addProperty("enableVillagers", getBooleanOrDefault(settings, "enableVillagers", defaultData.settings.enableVillagers));
                settings.addProperty("keepVillagerGender", getBooleanOrDefault(settings, "keepVillagerGender", defaultData.settings.keepVillagerGender));
            }
            
            if (root.has("autoScanComplete")) {
                root.addProperty("autoScanComplete", getBooleanOrDefault(root, "autoScanComplete", defaultData.autoScanComplete));
            }
            
            data = GSON.fromJson(root, GenderData.class);
            validate();
            save();
            GenderMod.LOGGER.info("Config loaded and fixed successfully");
        } catch (Exception e) {
            GenderMod.LOGGER.error("Failed to load config", e);
            createBackupAndNewConfig();
        }
    }
    
    private static int getIntOrDefault(JsonObject obj, String key, int defaultValue) {
        if (!obj.has(key)) return defaultValue;
        try {
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return el.getAsInt();
            }
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                try {
                    return Integer.parseInt(el.getAsString());
                } catch (NumberFormatException ignored) {}
            }
        } catch (Exception ignored) {}
        GenderMod.LOGGER.warn("Invalid value for '{}', using default: {}", key, defaultValue);
        return defaultValue;
    }
    
    private static int getIntWithBounds(JsonObject obj, String key, int defaultValue, int min, int max) {
        int value = getIntOrDefault(obj, key, defaultValue);
        if (value < min) {
            GenderMod.LOGGER.warn("Value for '{}' {} is below minimum {}, clamping to {}", key, value, min, min);
            return min;
        }
        if (value > max) {
            GenderMod.LOGGER.warn("Value for '{}' {} is above maximum {}, clamping to {}", key, value, max, max);
            return max;
        }
        return value;
    }
    
    private static boolean getBooleanOrDefault(JsonObject obj, String key, boolean defaultValue) {
        if (!obj.has(key)) return defaultValue;
        try {
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
                return el.getAsBoolean();
            }
        } catch (Exception ignored) {}
        GenderMod.LOGGER.warn("Invalid value for '{}', using default: {}", key, defaultValue);
        return defaultValue;
    }
    
    private static void createBackupAndNewConfig() {
        try {
            File brokenFile = CONFIG_PATH.toFile();
            if (brokenFile.exists()) {
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String backupName = "genderbub_backup_" + timestamp + ".json";
                Path backupPath = BACKUP_DIR.resolve(backupName);
                
                java.nio.file.Files.copy(brokenFile.toPath(), backupPath);
                GenderMod.LOGGER.info("Backup saved: {}", backupPath);
                
                cleanOldBackups();
            }
        } catch (Exception ex) {
            GenderMod.LOGGER.error("Failed to create backup", ex);
        }
        
        createDefault();
    }
    
    private static void cleanOldBackups() {
        try {
            File[] backups = BACKUP_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            if (backups == null) return;
            
            java.util.Arrays.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            
            for (int i = MAX_BACKUPS; i < backups.length; i++) {
                backups[i].delete();
                GenderMod.LOGGER.info("Deleted old backup: {}", backups[i].getName());
            }
        } catch (Exception e) {
            GenderMod.LOGGER.error("Failed to clean old backups", e);
        }
    }
    
    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(data, writer);
        } catch (Exception e) {
            GenderMod.LOGGER.error("Failed to save config", e);
        }
    }
    
    public static void reset() {
        data = new GenderData();
        save();
    }
    
    private static void createDefault() {
        data = new GenderData();
        save();
        GenderMod.LOGGER.info("Created new default config");
    }
    
    private static void validate() {
        GenderData.Settings s = data.settings;
        
        if (s.maleChance > 50) s.maleChance = 50;
        if (s.maleChance < 0) s.maleChance = 0;
        if (s.femaleChance > 50) s.femaleChance = 50;
        if (s.femaleChance < 0) s.femaleChance = 0;
        
        if (s.displayRadius < 0) s.displayRadius = 0;
        if (s.displayRadius > 128) s.displayRadius = 128;
        
        if (s.enabledMobs == null) s.enabledMobs = new java.util.ArrayList<>();
        if (s.genderOnlyMobs == null) s.genderOnlyMobs = new java.util.ArrayList<>();
        if (data.interactions == null) data.interactions = new java.util.ArrayList<>();
        if (data.eggRules == null) data.eggRules = new java.util.ArrayList<>();
        
        validateInteractions();
        validateEggRules();
    }
    
    private static void validateInteractions() {
        if (data.interactions == null) {
            data.interactions = new java.util.ArrayList<>();
            return;
        }
        
        java.util.List<GenderData.InteractionRule> validRules = new java.util.ArrayList<>();
        
        for (GenderData.InteractionRule rule : data.interactions) {
            if (rule == null) continue;
            if (rule.mobId == null || rule.mobId.isEmpty()) continue;
            if (rule.genders == null || rule.genders.isEmpty()) continue;
            if (rule.itemIds == null || rule.itemIds.isEmpty()) continue;
            
            java.util.List<String> validGenders = new java.util.ArrayList<>();
            for (String g : rule.genders) {
                if (g != null && (g.equals("any") || g.equals("male") || g.equals("female") || g.equals("sterile"))) {
                    validGenders.add(g);
                }
            }
            if (validGenders.isEmpty()) continue;
            rule.genders = validGenders;
            
            java.util.List<String> validItems = new java.util.ArrayList<>();
            for (String item : rule.itemIds) {
                if (item != null && !item.isEmpty()) {
                    validItems.add(item);
                }
            }
            if (validItems.isEmpty()) continue;
            rule.itemIds = validItems;
            
            validRules.add(rule);
        }
        
        if (validRules.size() != data.interactions.size()) {
            GenderMod.LOGGER.warn("Interactions: {} invalid rules removed, {} valid rules remain", 
                data.interactions.size() - validRules.size(), validRules.size());
        }
        data.interactions = validRules;
    }
    
    private static void validateEggRules() {
        if (data.eggRules == null) {
            data.eggRules = new java.util.ArrayList<>();
            return;
        }
        
        java.util.List<GenderData.EggRule> validRules = new java.util.ArrayList<>();
        
        for (GenderData.EggRule rule : data.eggRules) {
            if (rule == null) continue;
            if (rule.mobId == null || rule.mobId.isEmpty()) continue;
            if (rule.genders == null || rule.genders.isEmpty()) continue;
            
            java.util.List<String> validGenders = new java.util.ArrayList<>();
            for (String g : rule.genders) {
                if (g != null && (g.equals("any") || g.equals("male") || g.equals("female") || g.equals("sterile"))) {
                    validGenders.add(g);
                }
            }
            if (validGenders.isEmpty()) continue;
            rule.genders = validGenders;
            
            validRules.add(rule);
        }
        
        if (validRules.size() != data.eggRules.size()) {
            GenderMod.LOGGER.warn("EggRules: {} invalid rules removed, {} valid rules remain", 
                data.eggRules.size() - validRules.size(), validRules.size());
        }
        data.eggRules = validRules;
    }
    
    public static GenderData getData() { return data; }
    
    public static Path getConfigPath() { return CONFIG_PATH; }
}