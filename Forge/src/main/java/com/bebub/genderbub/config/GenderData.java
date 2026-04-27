package com.bebub.genderbub.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import java.util.ArrayList;
import java.util.List;

public class GenderData {
    
    public Settings settings = new Settings();
    public List<InteractionRule> interactions = new ArrayList<>();
    public List<EggRule> eggRules = new ArrayList<>();
    public boolean autoScanComplete = false;
    
    public static class Settings {
        public int maleChance = 45;
        public int femaleChance = 45;
        public int displayRadius = 24;
        public boolean hideWithJade = true;
        public boolean requireScanner = true;
        public boolean syncConfigRules = false;
        public boolean allowMaleMaleBreed = false;
        public boolean allowFemaleFemaleBreed = false;
        public boolean allowSterileBreed = false;
        public boolean enableVillagers = true;
        public boolean keepVillagerGender = true;
        public List<String> enabledMobs = new ArrayList<>();
        public List<String> genderOnlyMobs = new ArrayList<>();
    }
    
    public static class InteractionRule {
        public String mobId;
        public List<String> genders;
        public List<String> itemIds;
        
        public InteractionRule() {}
        
        public InteractionRule(String mobId, List<String> genders, List<String> itemIds) {
            this.mobId = mobId;
            this.genders = genders;
            this.itemIds = itemIds;
        }
        
        public boolean isGenderMatch(String gender, boolean sterile) {
            for (String g : genders) {
                if (g.equals("any")) return true;
                if (g.equals("male") && (gender.equals("male") || (sterile && gender.equals("male")))) return true;
                if (g.equals("female") && (gender.equals("female") || (sterile && gender.equals("female")))) return true;
                if (g.equals("sterile") && sterile) return true;
            }
            return false;
        }
        
        public boolean isItemMatch(String itemId) {
            for (String id : itemIds) {
                if (id.equals(itemId)) return true;
                if (id.startsWith("#")) {
                    TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.tryParse(id.substring(1)));
                    ResourceLocation itemLoc = ResourceLocation.tryParse(itemId);
                    if (tag != null && itemLoc != null) {
                        Item item = BuiltInRegistries.ITEM.get(itemLoc);
                        if (item != null && item.builtInRegistryHolder().is(tag)) return true;
                    }
                }
            }
            return false;
        }
    }
    
    public static class EggRule {
        public String mobId;
        public List<String> genders;
        
        public EggRule() {}
        
        public EggRule(String mobId, List<String> genders) {
            this.mobId = mobId;
            this.genders = genders;
        }
        
        public boolean isGenderMatch(String gender, boolean sterile) {
            for (String g : genders) {
                if (g.equals("any")) return true;
                if (g.equals("male") && gender.equals("male")) return true;
                if (g.equals("female") && gender.equals("female")) return true;
                if (g.equals("sterile") && sterile) return true;
            }
            return false;
        }
    }
}