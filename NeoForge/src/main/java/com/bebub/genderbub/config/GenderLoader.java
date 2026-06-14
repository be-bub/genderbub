package com.bebub.genderbub.config;

import com.bebub.genderbub.GenderMod;
import com.bebub.genderbub.compat.GenderAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.*;

public class GenderLoader {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "genderbub", "genderbub.json");
    private static final Path BACKUP_DIR = Path.of("config", "genderbub", "backups");
    private static final Path INTEGRATION_DIR = Path.of("config", "genderbub", "integration");
    private static final Path COMPAT_DIR = INTEGRATION_DIR.resolve("compat");
    private static final Path RULES_DIR = INTEGRATION_DIR.resolve("rules");
    private static final int MAX_BACKUPS = 10;
    private static GenderData data;
    
    public static void init() {
        CONFIG_PATH.getParent().toFile().mkdirs();
        BACKUP_DIR.toFile().mkdirs();
        INTEGRATION_DIR.toFile().mkdirs();
        COMPAT_DIR.toFile().mkdirs();
        RULES_DIR.toFile().mkdirs();
        
        mergeDefaultFiles();
        
        load();
        loadCompatRules();
        loadRules();
    }
    
    public static void copyDefaultFiles() {
        mergeDefaultFiles();
    }
    
    public static void mergeDefaultFiles() {
        mergeFile("integration/compat/environmental.json", COMPAT_DIR.resolve("environmental.json"));
        mergeFile("integration/compat/iceandfire.json", COMPAT_DIR.resolve("iceandfire.json"));
        mergeFile("integration/compat/mca.json", COMPAT_DIR.resolve("mca.json"));
        mergeFile("integration/compat/naturalist.json", COMPAT_DIR.resolve("naturalist.json"));
        mergeFile("integration/compat/primal.json", COMPAT_DIR.resolve("primal.json"));
        mergeFile("integration/compat/villagers_reborn.json", COMPAT_DIR.resolve("villagers_reborn.json"));
        
        mergeFile("integration/rules/alexsmobs.json", RULES_DIR.resolve("alexsmobs.json"));
        mergeFile("integration/rules/environmental.json", RULES_DIR.resolve("environmental.json"));
        mergeFile("integration/rules/meadow.json", RULES_DIR.resolve("meadow.json"));
        mergeFile("integration/rules/minecraft.json", RULES_DIR.resolve("minecraft.json"));
        mergeFile("integration/rules/naturalist.json", RULES_DIR.resolve("naturalist.json"));
    }
    
    private static void mergeFile(String resourcePath, Path targetPath) {
        try {
            String defaultJson = readResourceFile(resourcePath);
            if (defaultJson == null) return;
            
            if (!targetPath.toFile().exists()) {
                Files.writeString(targetPath, defaultJson);
                return;
            }
            
            String existingJson = Files.readString(targetPath);
            String merged = mergeJson(defaultJson, existingJson);
            
            if (!merged.equals(existingJson)) {
                Files.writeString(targetPath, merged);
            }
        } catch (Exception ignored) {}
    }
    
    private static String readResourceFile(String path) {
        try (InputStream in = GenderLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static String mergeJson(String defaultJson, String targetJson) {
        try {
            JsonObject defaultRoot = JsonParser.parseString(defaultJson).getAsJsonObject();
            JsonObject targetRoot = JsonParser.parseString(targetJson).getAsJsonObject();
            
            if (!defaultRoot.has("mobs") || !targetRoot.has("mobs")) {
                return targetJson;
            }
            
            Set<String> existingMobIds = new HashSet<>();
            for (JsonElement elem : targetRoot.getAsJsonArray("mobs")) {
                JsonObject mob = elem.getAsJsonObject();
                if (mob.has("mobId")) {
                    existingMobIds.add(mob.get("mobId").getAsString());
                }
            }
            
            for (JsonElement elem : defaultRoot.getAsJsonArray("mobs")) {
                JsonObject defaultMob = elem.getAsJsonObject();
                String mobId = defaultMob.get("mobId").getAsString();
                
                if (!existingMobIds.contains(mobId)) {
                    targetRoot.getAsJsonArray("mobs").add(defaultMob);
                }
            }
            
            return GSON.toJson(targetRoot);
        } catch (Exception e) {
            return targetJson;
        }
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
        } catch (Exception e) {
            createBackupAndNewConfig();
        }
    }
    
    public static void reloadAll() {
        mergeDefaultFiles();
        load();
        loadCompatRules();
        loadRules();
    }
    
    public static void loadCompatRules() {
        List<MobCompatRule> rules = new ArrayList<>();
        if (!COMPAT_DIR.toFile().exists()) return;
        
        try {
            Files.list(COMPAT_DIR).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                try {
                    String json = Files.readString(path);
                    CompatFile file = GSON.fromJson(json, CompatFile.class);
                    if (file.mobs != null) {
                        rules.addAll(file.mobs);
                    }
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
        
        GenderAddon.loadRules(rules);
    }
    
    public static void loadRules() {
        List<ScanRule> rules = new ArrayList<>();
        if (!RULES_DIR.toFile().exists()) return;
        
        try {
            Files.list(RULES_DIR).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                try {
                    String json = Files.readString(path);
                    RulesFile file = GSON.fromJson(json, RulesFile.class);
                    if (file.mobs != null) {
                        rules.addAll(file.mobs);
                    }
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
        
        GenderScanner.loadRules(rules);
    }
    
    private static int getIntOrDefault(JsonObject obj, String key, int defaultValue) {
        if (!obj.has(key)) return defaultValue;
        try {
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return el.getAsInt();
            }
        } catch (Exception ignored) {}
        return defaultValue;
    }
    
    private static int getIntWithBounds(JsonObject obj, String key, int defaultValue, int min, int max) {
        int value = getIntOrDefault(obj, key, defaultValue);
        if (value < min) return min;
        if (value > max) return max;
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
        return defaultValue;
    }
    
    private static void createBackupAndNewConfig() {
        try {
            File brokenFile = CONFIG_PATH.toFile();
            if (brokenFile.exists()) {
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String backupName = "genderbub_backup_" + timestamp + ".json";
                Path backupPath = BACKUP_DIR.resolve(backupName);
                Files.copy(brokenFile.toPath(), backupPath);
                cleanOldBackups();
            }
        } catch (Exception ignored) {}
        createDefault();
    }
    
    private static void cleanOldBackups() {
        try {
            File[] backups = BACKUP_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            if (backups == null) return;
            Arrays.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            for (int i = MAX_BACKUPS; i < backups.length; i++) {
                backups[i].delete();
            }
        } catch (Exception ignored) {}
    }
    
    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(data, writer);
        } catch (Exception ignored) {}
    }
    
    public static void reset() {
        data = new GenderData();
        save();
    }
    
    private static void createDefault() {
        data = new GenderData();
        save();
    }
    
    private static void validate() {
        GenderData.Settings s = data.settings;
        if (s.maleChance > 50) s.maleChance = 50;
        if (s.maleChance < 0) s.maleChance = 0;
        if (s.femaleChance > 50) s.femaleChance = 50;
        if (s.femaleChance < 0) s.femaleChance = 0;
        if (s.displayRadius < 0) s.displayRadius = 0;
        if (s.displayRadius > 128) s.displayRadius = 128;
        if (s.enabledMobs == null) s.enabledMobs = new ArrayList<>();
        if (s.genderOnlyMobs == null) s.genderOnlyMobs = new ArrayList<>();
        if (data.interactions == null) data.interactions = new ArrayList<>();
        if (data.eggRules == null) data.eggRules = new ArrayList<>();
        validateInteractions();
        validateEggRules();
    }
    
    private static void validateInteractions() {
        List<GenderData.InteractionRule> validRules = new ArrayList<>();
        for (GenderData.InteractionRule rule : data.interactions) {
            if (rule == null) continue;
            if (rule.mobId == null || rule.mobId.isEmpty()) continue;
            if (rule.genders == null || rule.genders.isEmpty()) continue;
            if (rule.itemIds == null || rule.itemIds.isEmpty()) continue;
            validRules.add(rule);
        }
        data.interactions = validRules;
    }
    
    private static void validateEggRules() {
        List<GenderData.EggRule> validRules = new ArrayList<>();
        for (GenderData.EggRule rule : data.eggRules) {
            if (rule == null) continue;
            if (rule.mobId == null || rule.mobId.isEmpty()) continue;
            if (rule.genders == null || rule.genders.isEmpty()) continue;
            validRules.add(rule);
        }
        data.eggRules = validRules;
    }
    
    public static GenderData getData() { return data; }
    public static Path getConfigPath() { return CONFIG_PATH; }
    
    public static class CompatFile {
        public List<MobCompatRule> mobs;
    }
    
    public static class RulesFile {
        public List<ScanRule> mobs;
    }
    
    public static class MobCompatRule {
        public String mobId;
        public List<CompatRule> rules;
        public String forceGender;
        public int sterileChance = -1;
        public String sterileMethod;
        public boolean sterileInvert;
        public ColorConfig colors;
    }
    
    public static class CompatRule {
        public String method;
        public boolean expected;
        public String gender;
    }
    
    public static class ColorConfig {
        public Integer male;
        public Integer female;
        public Integer sterile;
        public Integer baby;
    }
    
    public static class ScanRule {
        public String mobId;
        public List<String> eggRule;
        public InteractionRuleConfig interactionRule;
    }
    
    public static class InteractionRuleConfig {
        public List<String> genders;
        public List<String> items;
    }
}