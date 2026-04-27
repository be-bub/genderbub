package com.bebub.genderbub.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import java.util.List;
import java.util.Random;

public class GenderMatcher {
    
    private static final Random RANDOM = new Random();
    
    public static boolean isScannerItem(Item item) {
        if (item == null) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id != null && id.toString().equals("genderbub:magnifying_glass");
    }
    
    public static String[] getRandomGenderWithSterile(int maleChance, int femaleChance) {
        int male = Math.min(maleChance, 50);
        int female = Math.min(femaleChance, 50);
        if (male < 0) male = 0;
        if (female < 0) female = 0;
        
        if (male == 0 && female == 0) return new String[]{"none", "false"};
        if (male == 0) return new String[]{"female", "false"};
        if (female == 0) return new String[]{"male", "false"};
        
        int total = male + female;
        if (total > 100) {
            male = male * 100 / total;
            female = 100 - male;
        }
        
        int roll = RANDOM.nextInt(100);
        if (roll < male) return new String[]{"male", "false"};
        if (roll < male + female) return new String[]{"female", "false"};
        
        String gender = male > female ? "male" : (female > male ? "female" : (RANDOM.nextBoolean() ? "male" : "female"));
        return new String[]{gender, "true"};
    }
}