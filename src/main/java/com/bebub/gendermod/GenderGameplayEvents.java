package com.bebub.gendermod;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.eventbus.api.Event;
import com.bebub.gendermod.config.Config;
import com.bebub.gendermod.network.GenderSyncPacket;
import com.bebub.gendermod.network.NetworkHandler;

import java.util.List;
import java.util.UUID;
import java.util.Random;
import java.util.stream.Collectors;

public class GenderGameplayEvents {
    private static final ParticleOptions BLOCKED_BREEDING_PARTICLES = ParticleTypes.ANGRY_VILLAGER;
    private static List<String> enabledMobs;
    private static List<GenderInteractionRule> genderRules;
    private static Random random = new Random();

    public static void setConfiguration(List<String> mobs, List<Config.GenderRuleConfig> rules) {
        enabledMobs = mobs;
        genderRules = rules.stream().map(GenderInteractionRule::new).collect(Collectors.toList());
    }

    private String getGenderFromNBT(Animal animal) {
        CompoundTag nbt = animal.getPersistentData();
        return nbt.contains("GenderMod_Gender") ? nbt.getString("GenderMod_Gender") : null;
    }

    private String getRandomGender() {
        int maleChance = Config.getMaleChance();
        int roll = random.nextInt(100);
        return roll < maleChance ? "male" : "female";
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        
        Entity entity = event.getEntity();
        if (entity instanceof Animal animal) {
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(animal.getType());
            if (entityId != null && enabledMobs != null && enabledMobs.contains(entityId.toString())) {
                CompoundTag nbt = animal.getPersistentData();
                if (!nbt.contains("GenderMod_Gender")) {
                    String gender = getRandomGender();
                    nbt.putString("GenderMod_Gender", gender);
                }
            }
        }
    }

    @SubscribeEvent
    public void onAnimalBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA() instanceof Animal parent1 && event.getParentB() instanceof Animal parent2) {
            String gender1 = getGenderFromNBT(parent1);
            String gender2 = getGenderFromNBT(parent2);
            if (gender1 != null && gender2 != null && gender1.equals(gender2)) {
                event.setCanceled(true);
                Level level = parent1.level();
                if (!level.isClientSide) {
                    spawnAngryParticles(parent1, (ServerLevel)level);
                    spawnAngryParticles(parent2, (ServerLevel)level);
                }
            }
        }
    }

    private void spawnAngryParticles(Animal animal, ServerLevel level) {
        for (int i = 0; i < 5; i++) {
            Vec3 pos = animal.position().add(0, 1.5, 0).add(level.random.nextGaussian() * 0.5, 0, level.random.nextGaussian() * 0.5);
            level.sendParticles(BLOCKED_BREEDING_PARTICLES, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Item item = event.getItemStack().getItem();
        if (item != GenderMod.GENDER_SCANNER.get()) return;

        if (event.getTarget() instanceof Animal animal) {
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(animal.getType());
            String gender = getGenderFromNBT(animal);
            if (entityId != null && gender != null) {
                if (shouldCancelInteraction(entityId, gender, item)) {
                    event.setCanceled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }

                if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new GenderSyncPacket(animal.getUUID(), gender)
                    );
                    event.setCanceled(true);
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }

    private boolean shouldCancelInteraction(ResourceLocation entityId, String gender, Item item) {
        return genderRules.stream().anyMatch(rule -> rule.matches(entityId, gender, item));
    }
}