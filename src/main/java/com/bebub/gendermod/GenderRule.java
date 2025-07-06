package com.bebub.gendermod;

import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class GenderRule {
    private final String mobId;
    private final String gender;
    private final String itemId;

    public GenderRule(String ruleString) {
        String[] parts = ruleString.split(",");
        if (parts.length == 3) {
            this.mobId = parts[0].trim();
            this.gender = parts[1].trim();
            this.itemId = parts[2].trim();
        } else {
            this.mobId = "";
            this.gender = "";
            this.itemId = "";
        }
    }

    public boolean matches(ResourceLocation entityId, String gender, Item item) {
        if (!entityId.toString().equals(mobId) || !gender.equals(this.gender)) {
            return false;
        }
        ResourceLocation itemResource = ForgeRegistries.ITEMS.getKey(item);
        return itemResource != null && itemResource.toString().equals(itemId);
    }
}