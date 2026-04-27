package com.bebub.genderbub.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
        
        int maleSterile = (male == 0) ? 0 : (50 - male);
        int femaleSterile = (female == 0) ? 0 : (50 - female);
        
        int total = male + maleSterile + female + femaleSterile;
        
        if (total == 0) {
            return new String[]{"none", "false"};
        }
        
        int roll = RANDOM.nextInt(total);
        
        if (roll < male) {
            return new String[]{"male", "false"};
        }
        
        if (roll < male + maleSterile) {
            return new String[]{"male", "true"};
        }
        
        if (roll < male + maleSterile + female) {
            return new String[]{"female", "false"};
        }
        
        return new String[]{"female", "true"};
    }
}