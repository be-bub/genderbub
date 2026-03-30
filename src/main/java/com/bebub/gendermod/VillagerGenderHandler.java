package com.bebub.genderbub;

import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = GenderMod.MOD_ID)
public class VillagerGenderHandler {
    
    public static String getVillagerGender(Villager villager) {
        CompoundTag nbt = villager.getPersistentData();
        return nbt.contains("GenderMod_Gender") ? nbt.getString("GenderMod_Gender") : null;
    }
    
    public static void setVillagerGender(Villager villager, String gender) {
        villager.getPersistentData().putString("GenderMod_Gender", gender);
    }
    
    @SubscribeEvent
    public static void onVillagerJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Villager villager)) return;
        
        if (!GenderConfig.isVillagerEnabled()) return;
        
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(villager.getType());
        if (id == null) return;
        
        if (!GenderConfig.getEnabledVillagers().contains(id.toString())) return;
        
        if (!villager.getPersistentData().contains("GenderMod_Gender")) {
            String[] result = GenderConfig.getRandomVillagerGender();
            setVillagerGender(villager, result[0]);
        }
    }
    
    @SubscribeEvent
    public static void onVillagerBreed(BabyEntitySpawnEvent event) {
        if (!(event.getParentA() instanceof Villager a) || !(event.getParentB() instanceof Villager b)) return;
        
        if (!GenderConfig.isVillagerEnabled()) return;
        
        String g1 = getVillagerGender(a);
        String g2 = getVillagerGender(b);
        
        if (g1 == null || g2 == null) return;
        
        if (g1.equals(g2)) {
            event.setCanceled(true);
            Level level = a.level();
            if (level instanceof ServerLevel sl) {
                GenderGameplayEvents.spawnAngryParticles(a, sl);
                GenderGameplayEvents.spawnAngryParticles(b, sl);
            }
        } else {
            CompoundTag childData = event.getChild().getPersistentData();
            String childGender = new java.util.Random().nextBoolean() ? "male" : "female";
            childData.putString("GenderMod_Gender", childGender);
        }
    }
}