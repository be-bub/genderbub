package com.bebub.genderbub.config;

import com.bebub.genderbub.GenderMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class GenderScanner {
    
    private static final Map<String, GenderLoader.ScanRule> RULES = new HashMap<>();
    private static boolean firstScanDone = false;
    
    private static final Set<String> ICEANDFIRE_WHITELIST = new HashSet<>(Arrays.asList(
        "iceandfire:fire_dragon",
        "iceandfire:ice_dragon",
        "iceandfire:lightning_dragon",
        "iceandfire:hippogryph",
        "iceandfire:amphithere",
        "iceandfire:hippocampus"
    ));
    
    public static void loadRules(List<GenderLoader.ScanRule> rules) {
        RULES.clear();
        for (GenderLoader.ScanRule rule : rules) {
            if (rule.mobId != null) {
                RULES.put(rule.mobId, rule);
            }
        }
    }
    
    public static void performFirstScan(GenderData data) {
        if (!data.autoScanComplete && !firstScanDone) {
            firstScanDone = true;
            scanAndAdd(data);
            data.autoScanComplete = true;
            GenderLoader.save();
        }
    }
    
    public static List<String> scanAndGetNew(GenderData data) {
        List<String> validAnimals = filterAnimals(getAllMobs());
        List<String> newAnimals = new ArrayList<>();
        Set<String> existing = new HashSet<>(data.settings.enabledMobs);
        boolean changes = false;
        
        for (String animal : validAnimals) {
            if (!data.settings.enabledMobs.contains(animal)) {
                newAnimals.add(animal);
                data.settings.enabledMobs.add(animal);
                changes = true;
            }
        }
        
        if (addRulesFromConfig(data, existing)) {
            changes = true;
        }
        
        if (changes) {
            sortEnabledMobs(data);
            GenderLoader.save();
        }
        return newAnimals;
    }
    
    private static void scanAndAdd(GenderData data) {
        for (String animal : filterAnimals(getAllMobs())) {
            if (!data.settings.enabledMobs.contains(animal)) {
                data.settings.enabledMobs.add(animal);
            }
        }
        
        if (ModList.get().isLoaded("iceandfire")) {
            for (String id : ICEANDFIRE_WHITELIST) {
                if (!data.settings.enabledMobs.contains(id)) {
                    data.settings.enabledMobs.add(id);
                }
            }
        }
        
        addRulesFromConfig(data, new HashSet<>(data.settings.enabledMobs));
        sortEnabledMobs(data);
        GenderLoader.save();
    }
    
    private static boolean addRulesFromConfig(GenderData data, Set<String> existing) {
        boolean changed = false;
        
        for (Map.Entry<String, GenderLoader.ScanRule> entry : RULES.entrySet()) {
            String mobId = entry.getKey();
            var rule = entry.getValue();
            
            if (!existing.contains(mobId)) continue;
            
            String modId = mobId.contains(":") ? mobId.split(":")[0] : "";
            if (!modId.isEmpty() && !modId.equals("minecraft") && !ModList.get().isLoaded(modId)) {
                continue;
            }
            
            if (rule.eggRule != null && !rule.eggRule.isEmpty()) {
                if (data.eggRules.stream().noneMatch(r -> r.mobId.equals(mobId))) {
                    data.eggRules.add(new GenderData.EggRule(mobId, rule.eggRule));
                    changed = true;
                }
            }
            
            if (rule.interactionRule != null && rule.interactionRule.genders != null && !rule.interactionRule.genders.isEmpty()) {
                if (data.interactions.stream().noneMatch(r -> r.mobId.equals(mobId))) {
                    List<String> validItems = new ArrayList<>();
                    if (rule.interactionRule.items != null) {
                        for (String itemId : rule.interactionRule.items) {
                            boolean conditional = itemId.startsWith("+");
                            String realItemId = conditional ? itemId.substring(1) : itemId;
                            String itemModId = realItemId.contains(":") ? realItemId.split(":")[0] : "minecraft";
                            
                            if (conditional) {
                                if (itemModId.equals("minecraft") || ModList.get().isLoaded(itemModId)) {
                                    validItems.add(realItemId);
                                }
                            } else {
                                validItems.add(realItemId);
                            }
                        }
                    }
                    if (!validItems.isEmpty()) {
                        data.interactions.add(new GenderData.InteractionRule(mobId, rule.interactionRule.genders, validItems));
                        changed = true;
                    }
                }
            }
        }
        
        return changed;
    }
    
    private static List<String> getAllMobs() {
        List<String> all = new ArrayList<>();
        for (var entry : ForgeRegistries.ENTITY_TYPES.getEntries()) {
            all.add(entry.getKey().location().toString());
        }
        Collections.sort(all);
        return all;
    }
    
    private static List<String> filterAnimals(List<String> allMobs) {
        List<String> animals = new ArrayList<>();
        for (String mobId : allMobs) {
            var loc = new net.minecraft.resources.ResourceLocation(mobId);
            var type = ForgeRegistries.ENTITY_TYPES.getValue(loc);
            if (type != null && isAnimal(type, mobId)) {
                animals.add(mobId);
            }
        }
        Collections.sort(animals);
        return animals;
    }
    
    private static boolean isAnimal(EntityType<?> type, String id) {
        if (isBlacklisted(id)) return false;
        
        try { if (type.getBaseClass() != null && Animal.class.isAssignableFrom(type.getBaseClass())) return true; } catch (Exception e) {}
        try { if (type.getBaseClass() != null && WaterAnimal.class.isAssignableFrom(type.getBaseClass())) return true; } catch (Exception e) {}
        
        if (type.getCategory() == MobCategory.CREATURE || 
            type.getCategory() == MobCategory.WATER_CREATURE || 
            type.getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE) return true;
        
        try { if (type.getBaseClass() != null && AbstractHorse.class.isAssignableFrom(type.getBaseClass())) return true; } catch (Exception e) {}
        try { if (type.getBaseClass() != null && AbstractGolem.class.isAssignableFrom(type.getBaseClass())) return false; } catch (Exception e) {}
        
        if (type.getCategory() == MobCategory.MONSTER || 
            type.getCategory() == MobCategory.MISC || 
            type.getCategory() == MobCategory.AMBIENT) return false;
        
        return false;
    }
    
    private static boolean isBlacklisted(String id) {
        String[] hostile = {"zombie", "skeleton", "spider", "creeper", "enderman", "witch", "pillager", "wither", "dragon", "blaze", "ghast", "slime", "magma_cube"};
        for (String h : hostile) if (id.contains(h)) return true;
        
        String[] nonLiving = {"arrow", "fireball", "potion", "trident", "snowball", "egg", "boat", "minecart", "item_frame", "painting", "tnt", "falling_block"};
        for (String n : nonLiving) if (id.contains(n)) return true;
        
        if (id.contains("_part") || id.contains("_segment") || id.contains("piece")) return true;
        if (id.equals("minecraft:player") || id.equals("minecraft:armor_stand")) return true;
        
        return false;
    }
    
    private static void sortEnabledMobs(GenderData data) {
        data.settings.enabledMobs.sort((a, b) -> {
            String modA = a.contains(":") ? a.split(":")[0] : "";
            String modB = b.contains(":") ? b.split(":")[0] : "";
            int cmp = modA.compareTo(modB);
            if (cmp != 0) return cmp;
            String nameA = a.contains(":") ? a.split(":")[1] : a;
            String nameB = b.contains(":") ? b.split(":")[1] : b;
            return nameA.compareTo(nameB);
        });
    }
}