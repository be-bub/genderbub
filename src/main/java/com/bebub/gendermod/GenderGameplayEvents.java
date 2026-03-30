package com.bebub.genderbub;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.InteractionResult;
import com.bebub.genderbub.config.GenderConfig;
import com.bebub.genderbub.network.GenderSyncPacket;
import com.bebub.genderbub.network.NetworkHandler;

import java.util.*;

public class GenderGameplayEvents {
    private static final ParticleOptions BLOCKED_BREEDING_PARTICLES = ParticleTypes.ANGRY_VILLAGER;
    private static Set<String> enabledMobs;
    private static Map<String, List<GenderConfig.InteractionRule>> rulesByMob;

    public static void reloadConfig() {
        enabledMobs = new HashSet<>(GenderConfig.getEnabledMobs());
        rulesByMob = new HashMap<>();
        for (String mobId : enabledMobs) {
            List<GenderConfig.InteractionRule> rules = GenderConfig.getRulesForMob(mobId);
            if (!rules.isEmpty()) {
                rulesByMob.put(mobId, rules);
            }
        }
    }

    public static String getGender(Mob mob) {
        CompoundTag nbt = mob.getPersistentData();
        return nbt.contains("GenderMod_Gender") ? nbt.getString("GenderMod_Gender") : null;
    }
    
    public static boolean isSterile(Mob mob) {
        CompoundTag nbt = mob.getPersistentData();
        return nbt.contains("GenderMod_Sterile") && nbt.getBoolean("GenderMod_Sterile");
    }
    
    private void setSterile(Mob mob, boolean sterile) {
        mob.getPersistentData().putBoolean("GenderMod_Sterile", sterile);
    }

    private void setGender(Mob mob, String gender) {
        mob.getPersistentData().putString("GenderMod_Gender", gender);
    }

    public static boolean shouldCancelInteraction(Mob mob, Item item) {
        if (enabledMobs == null) return false;
        
        ResourceLocation mobId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (mobId == null) return false;
        
        String mobIdStr = mobId.toString();
        if (!enabledMobs.contains(mobIdStr)) return false;
        
        String gender = getGender(mob);
        if (gender == null) return false;
        
        boolean sterile = isSterile(mob);
        
        return GenderConfig.isItemBlocked(mobIdStr, gender, sterile, item);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (id == null || enabledMobs == null || !enabledMobs.contains(id.toString())) return;
        
        if (!mob.getPersistentData().contains("GenderMod_Gender")) {
            String[] result = GenderConfig.getRandomGenderWithSterile();
            setGender(mob, result[0]);
            setSterile(mob, Boolean.parseBoolean(result[1]));
        }
    }

    @SubscribeEvent
    public void onAnimalBreed(BabyEntitySpawnEvent event) {
        LivingEntity parentA = event.getParentA();
        LivingEntity parentB = event.getParentB();
        
        if (!(parentA instanceof Mob a) || !(parentB instanceof Mob b)) return;
        
        String g1 = getGender(a);
        String g2 = getGender(b);
        boolean sterile1 = isSterile(a);
        boolean sterile2 = isSterile(b);
        
        if (g1 == null || g2 == null) return;
        
        boolean cancel = false;
        
        if (sterile1 || sterile2) {
            if (!GenderConfig.isAllowSterileBreed()) {
                cancel = true;
            }
        } else if (g1.equals(g2)) {
            if (g1.equals("male") && !GenderConfig.isAllowMaleMaleBreed()) {
                cancel = true;
            } else if (g1.equals("female") && !GenderConfig.isAllowFemaleFemaleBreed()) {
                cancel = true;
            }
        }
        
        if (cancel) {
            event.setCanceled(true);
            Level level = a.level();
            if (!level.isClientSide && level instanceof ServerLevel sl) {
                spawnAngryParticles(a, sl);
                spawnAngryParticles(b, sl);
            }
        }
    }

    public static void spawnAngryParticles(Mob mob, ServerLevel level) {
        for (int i = 0; i < 5; i++) {
            Vec3 pos = mob.position().add(0, 1.5, 0)
                .add(level.random.nextGaussian() * 0.5, 0, level.random.nextGaussian() * 0.5);
            level.sendParticles(BLOCKED_BREEDING_PARTICLES, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getTarget() instanceof Mob mob)) return;
        
        Item item = event.getItemStack().getItem();
        
        if (shouldCancelInteraction(mob, item)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        
        if (item == GenderMod.GENDER_SCANNER.get()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                String gender = getGender(mob);
                boolean sterile = isSterile(mob);
                if (gender != null) {
                    NetworkHandler.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                        new GenderSyncPacket(mob.getUUID(), gender, sterile)
                    );
                    
                    Component displayComponent;
                    
                    if (mob instanceof Villager) {
                        if (mob.isBaby()) {
                            displayComponent = Component.translatable(
                                gender.equals("male") ? "genderbub.villager.boy" : "genderbub.villager.girl"
                            );
                        } else {
                            displayComponent = Component.translatable(
                                gender.equals("male") ? "genderbub.villager.male" : "genderbub.villager.female"
                            );
                        }
                    } else {
                        if (sterile) {
                            displayComponent = Component.translatable(
                                gender.equals("male") ? "genderbub.gender.sterile.male" : "genderbub.gender.sterile.female",
                                Component.translatable(gender.equals("male") ? "genderbub.gender.male" : "genderbub.gender.female")
                            );
                        } else {
                            displayComponent = Component.translatable(gender.equals("male") ? "genderbub.gender.male" : "genderbub.gender.female");
                        }
                    }
                    
                    player.displayClientMessage(displayComponent, true);
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getTarget() instanceof Mob mob)) return;
        
        Item item = event.getItemStack().getItem();
        
        if (shouldCancelInteraction(mob, item)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    public static boolean shouldBlockAction(Mob mob, String action) {
        if (enabledMobs == null) return false;
        
        ResourceLocation mobId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (mobId == null) return false;
        
        String mobIdStr = mobId.toString();
        if (!enabledMobs.contains(mobIdStr)) return false;
        
        String gender = getGender(mob);
        if (gender == null) return false;
        
        boolean sterile = isSterile(mob);
        
        return GenderConfig.isActionBlocked(mobIdStr, gender, sterile, action);
    }
}