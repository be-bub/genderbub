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
        
        for (String animal : validAnimals) {
            if (!data.settings.enabledMobs.contains(animal)) {
                newAnimals.add(animal);
                data.settings.enabledMobs.add(animal);
            }
        }
        
        addIceAndFireWhitelist(data, newAnimals);
        
        addDefaultRules(data, existing);
        
        if (!newAnimals.isEmpty()) {
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
        
        addDefaultRules(data, new HashSet<>(filterAnimals(getAllMobs())));
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
    
    private static void addDefaultRules(GenderData data, Set<String> existing) {
        if (existing.contains("minecraft:cow") && data.interactions.stream().noneMatch(r -> r.mobId.equals("minecraft:cow"))) {
            data.interactions.add(new GenderData.InteractionRule("minecraft:cow", Arrays.asList("male", "sterile"), Arrays.asList("minecraft:bucket")));
        }
        if (existing.contains("minecraft:goat") && data.interactions.stream().noneMatch(r -> r.mobId.equals("minecraft:goat"))) {
            data.interactions.add(new GenderData.InteractionRule("minecraft:goat", Arrays.asList("male", "sterile"), Arrays.asList("minecraft:bucket")));
        }
        if (existing.contains("minecraft:chicken") && data.eggRules.stream().noneMatch(r -> r.mobId.equals("minecraft:chicken"))) {
            data.eggRules.add(new GenderData.EggRule("minecraft:chicken", Arrays.asList("male", "sterile")));
        }
        if (existing.contains("alexsmobs:emu") && data.eggRules.stream().noneMatch(r -> r.mobId.equals("alexsmobs:emu"))) {
            data.eggRules.add(new GenderData.EggRule("alexsmobs:emu", Arrays.asList("male", "sterile")));
        }
        if (existing.contains("naturalist:duck") && data.eggRules.stream().noneMatch(r -> r.mobId.equals("naturalist:duck"))) {
            data.eggRules.add(new GenderData.EggRule("naturalist:duck", Arrays.asList("male", "sterile")));
        }
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