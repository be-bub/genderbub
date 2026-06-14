package com.bebub.genderbub;

import com.bebub.genderbub.compat.GenderAddon;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GenderMod.MOD_ID)
public class GenderEvents {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;
        
        GenderAddon.apply(entity);
        
        if (!GenderCore.getGender(entity).equals("none")) return;
        
        if (entity instanceof ZombieVillager) {
            if (GenderConfig.isEnableVillagers()) {
                String[] res = GenderConfig.getRandomGenderWithSterile();
                if (!res[0].equals("none")) {
                    GenderCore.setGender(entity, res[0]);
                    GenderCore.setSterile(entity, Boolean.parseBoolean(res[1]));
                }
            }
            return;
        }
        
        if (entity instanceof Villager) {
            if (!GenderConfig.isEnableVillagers()) return;
            String[] res = GenderConfig.getRandomGenderWithSterile();
            if (!res[0].equals("none")) {
                GenderCore.setGender(entity, res[0]);
                GenderCore.setSterile(entity, Boolean.parseBoolean(res[1]));
            }
            return;
        }
        
        boolean isAnimal = entity instanceof Animal;
        if (!isAnimal) return;
        
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (mobId == null) return;
        String mobIdStr = mobId.toString();
        
        boolean enabled = GenderConfig.isMobEnabled(mobIdStr);
        if (!enabled) return;
        
        if (GenderConfig.getGenderOnlyMobs().contains(mobIdStr)) {
            String[] res = GenderConfig.getRandomGenderWithSterile();
            if (!res[0].equals("none")) {
                GenderCore.setGender(entity, res[0]);
                GenderCore.setSterile(entity, false);
            }
            return;
        }
        
        String[] res = GenderConfig.getRandomGenderWithSterile();
        if (!res[0].equals("none")) {
            GenderCore.setGender(entity, res[0]);
            GenderCore.setSterile(entity, Boolean.parseBoolean(res[1]));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof LivingEntity entity)) return;
        
        if (shouldCancelInteraction(entity, event.getItemStack().getItem())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getTarget() instanceof LivingEntity entity)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        if (shouldCancelInteraction(entity, player.getMainHandItem().getItem())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        
        boolean isAnimal = entity instanceof Animal;
        boolean isVillager = entity instanceof Villager;
        boolean isZombieVillager = entity instanceof ZombieVillager;
        
        if (!isAnimal && !isVillager && !isZombieVillager) return;
        
        if ((isVillager || isZombieVillager) && !GenderConfig.isEnableVillagers()) return;
        
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (mobId == null) return;
        String mobIdStr = mobId.toString();
        
        if (!GenderConfig.isMobEnabled(mobIdStr)) return;
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (event.getChild() == null) return;
        if (event.getChild().level().isClientSide()) return;
        
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getChild().getType());
        if (mobId == null) return;
        
        String mobIdStr = mobId.toString();
        
        boolean isVillager = mobIdStr.equals("minecraft:villager");
        boolean isZombieVillager = mobIdStr.equals("minecraft:zombie_villager");
        
        if ((isVillager || isZombieVillager) && !GenderConfig.isEnableVillagers()) return;
        
        if (!GenderConfig.isMobEnabled(mobIdStr)) return;
        
        String[] result = GenderConfig.getRandomGenderWithSterile();
        if (!result[0].equals("none")) {
            GenderCore.setGender(event.getChild(), result[0]);
            GenderCore.setSterile(event.getChild(), Boolean.parseBoolean(result[1]));
        }
    }
    
    private static boolean shouldCancelInteraction(LivingEntity entity, Item item) {
        boolean isAnimal = entity instanceof Animal;
        boolean isVillager = entity instanceof Villager;
        boolean isZombieVillager = entity instanceof ZombieVillager;
        
        if (!isAnimal && !isVillager && !isZombieVillager) return false;
        
        if ((isVillager || isZombieVillager) && !GenderConfig.isEnableVillagers()) return false;
        
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (mobId == null) return false;
        String mobIdStr = mobId.toString();
        
        if (!GenderConfig.isMobEnabled(mobIdStr)) return false;
        
        String gender = GenderCore.getGender(entity);
        if (gender.equals("none")) return false;
        
        return GenderConfig.isItemBlocked(mobIdStr, gender, GenderCore.isSterile(entity), item);
    }
}