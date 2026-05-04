package com.bebub.genderbub.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.fml.ModList;
import java.util.*;

public class GenderScanner {
    
    private static boolean firstScanDone = false;
    
    private static final Set<String> ICEANDFIRE_WHITELIST = new HashSet<>(Arrays.asList(
        "iceandfire:fire_dragon",
        "iceandfire:ice_dragon",
        "iceandfire:lightning_dragon",
        "iceandfire:hippogryph",
        "iceandfire:amphithere",
        "iceandfire:hippocampus"
    ));
    
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
        Set<String> existing = new HashSet<>(validAnimals);
        boolean changes = false;
        
        for (String animal : validAnimals) {
            if (!data.settings.enabledMobs.contains(animal)) {
                newAnimals.add(animal);
                data.settings.enabledMobs.add(animal);
                changes = true;
            }
        }
        
        addIceAndFireWhitelist(data, newAnimals);
        
        if (addMissingRules(data, existing)) {
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
        
        addIceAndFireWhitelist(data, null);
        addMissingRules(data, new HashSet<>(filterAnimals(getAllMobs())));
        sortEnabledMobs(data);
        GenderLoader.save();
    }
    
    private static void addIceAndFireWhitelist(GenderData data, List<String> newAnimals) {
        if (!ModList.get().isLoaded("iceandfire")) return;
        
        for (String id : ICEANDFIRE_WHITELIST) {
            if (!data.settings.enabledMobs.contains(id)) {
                data.settings.enabledMobs.add(id);
                if (newAnimals != null) {
                    newAnimals.add(id);
                }
            }
        }
    }
    
    private static List<String> getAllMobs() {
        List<String> all = new ArrayList<>();
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            all.add(entry.getKey().location().toString());
        }
        Collections.sort(all);
        return all;
    }
    
    private static List<String> filterAnimals(List<String> allMobs) {
        List<String> animals = new ArrayList<>();
        for (String mobId : allMobs) {
            if (mobId.startsWith("iceandfire:")) continue;
            ResourceLocation loc = ResourceLocation.tryParse(mobId);
            if (loc != null) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(loc);
                if (type != null && isAnimal(type, mobId)) {
                    animals.add(mobId);
                }
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
    
    private static boolean addMissingRules(GenderData data, Set<String> existing) {
        boolean changed = false;
        List<String> buckets = new ArrayList<>(Arrays.asList("minecraft:bucket"));
        
        if (ModList.get().isLoaded("meadow")) {
            buckets.add("meadow:wooden_bucket");
        }
        
        if (existing.contains("minecraft:cow")) {
            GenderData.InteractionRule rule = data.interactions.stream().filter(r -> r.mobId.equals("minecraft:cow")).findFirst().orElse(null);
            if (rule == null) {
                data.interactions.add(new GenderData.InteractionRule("minecraft:cow", Arrays.asList("male", "sterile"), buckets));
                changed = true;
            } else if (!rule.itemIds.equals(buckets)) {
                rule.itemIds = buckets;
                changed = true;
            }
        }
        
        if (existing.contains("minecraft:goat")) {
            GenderData.InteractionRule rule = data.interactions.stream().filter(r -> r.mobId.equals("minecraft:goat")).findFirst().orElse(null);
            if (rule == null) {
                data.interactions.add(new GenderData.InteractionRule("minecraft:goat", Arrays.asList("male", "sterile"), buckets));
                changed = true;
            } else if (!rule.itemIds.equals(buckets)) {
                rule.itemIds = buckets;
                changed = true;
            }
        }
        
        if (existing.contains("environmental:yak")) {
            GenderData.InteractionRule rule = data.interactions.stream().filter(r -> r.mobId.equals("environmental:yak")).findFirst().orElse(null);
            if (rule == null) {
                data.interactions.add(new GenderData.InteractionRule("environmental:yak", Arrays.asList("male", "sterile"), buckets));
                changed = true;
            } else if (!rule.itemIds.equals(buckets)) {
                rule.itemIds = buckets;
                changed = true;
            }
        }
        
        if (existing.contains("meadow:water_buffalo")) {
            GenderData.InteractionRule rule = data.interactions.stream().filter(r -> r.mobId.equals("meadow:water_buffalo")).findFirst().orElse(null);
            if (rule == null) {
                data.interactions.add(new GenderData.InteractionRule("meadow:water_buffalo", Arrays.asList("male", "sterile"), buckets));
                changed = true;
            } else if (!rule.itemIds.equals(buckets)) {
                rule.itemIds = buckets;
                changed = true;
            }
        }
        
        if (existing.contains("meadow:wooly_cow")) {
            GenderData.InteractionRule rule = data.interactions.stream().filter(r -> r.mobId.equals("meadow:wooly_cow")).findFirst().orElse(null);
            if (rule == null) {
                data.interactions.add(new GenderData.InteractionRule("meadow:wooly_cow", Arrays.asList("male", "sterile"), buckets));
                changed = true;
            } else if (!rule.itemIds.equals(buckets)) {
                rule.itemIds = buckets;
                changed = true;
            }
        }
        
        if (existing.contains("minecraft:chicken")) {
            if (data.eggRules.stream().noneMatch(r -> r.mobId.equals("minecraft:chicken"))) {
                data.eggRules.add(new GenderData.EggRule("minecraft:chicken", Arrays.asList("male", "sterile")));
                changed = true;
            }
        }
        
        if (existing.contains("alexsmobs:emu")) {
            if (data.eggRules.stream().noneMatch(r -> r.mobId.equals("alexsmobs:emu"))) {
                data.eggRules.add(new GenderData.EggRule("alexsmobs:emu", Arrays.asList("male", "sterile")));
                changed = true;
            }
        }
        
        if (existing.contains("naturalist:duck")) {
            if (data.eggRules.stream().noneMatch(r -> r.mobId.equals("naturalist:duck"))) {
                data.eggRules.add(new GenderData.EggRule("naturalist:duck", Arrays.asList("male", "sterile")));
                changed = true;
            }
        }
        
        if (existing.contains("environmental:duck")) {
            if (data.eggRules.stream().noneMatch(r -> r.mobId.equals("environmental:duck"))) {
                data.eggRules.add(new GenderData.EggRule("environmental:duck", Arrays.asList("male", "sterile")));
                changed = true;
            }
        }
        
        return changed;
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