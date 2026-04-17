package com.bebub.genderbub;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ExternalMobHandler {
    
    public static boolean isNaturalistLion(LivingEntity entity) {
        String className = entity.getClass().getName();
        return className.equals("com.starfish_studios.naturalist.common.entity.Lion");
    }
    
    public static boolean isNaturalistLionBaby(LivingEntity entity) {
        if (entity instanceof AgeableMob) {
            return ((AgeableMob) entity).isBaby();
        }
        try {
            java.lang.reflect.Method isBaby = entity.getClass().getMethod("isBaby");
            return (boolean) isBaby.invoke(entity);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean naturalistLionHasMane(LivingEntity entity) {
        try {
            java.lang.reflect.Method hasMane = entity.getClass().getMethod("hasMane");
            return (boolean) hasMane.invoke(entity);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String determineNaturalistLionGender(LivingEntity entity) {
        if (isNaturalistLionBaby(entity)) {
            return null;
        }
        return naturalistLionHasMane(entity) ? "male" : "female";
    }
    
    public static boolean isPrimalLion(LivingEntity entity) {
        String className = entity.getClass().getName();
        return className.equals("org.primal.entity.animal.LionEntity");
    }
    
    public static boolean isPrimalLionBaby(LivingEntity entity) {
        try {
            java.lang.reflect.Method isBaby = entity.getClass().getMethod("isBaby");
            return (boolean) isBaby.invoke(entity);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isPrimalLionManeless(LivingEntity entity) {
        try {
            java.lang.reflect.Method isManeless = entity.getClass().getMethod("isManeless");
            return (boolean) isManeless.invoke(entity);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String determinePrimalLionGender(LivingEntity entity) {
        return !isPrimalLionManeless(entity) ? "male" : "female";
    }
    
    public static boolean isIceFireDragon(LivingEntity entity) {
        ResourceLocation regName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (regName == null) return false;
        String path = regName.getPath();
        return regName.getNamespace().equals("iceandfire") && (path.contains("dragon") || path.contains("Dragon"));
    }
    
    public static boolean isIceFireDragonBaby(LivingEntity entity) {
        if (entity instanceof AgeableMob) {
            return ((AgeableMob) entity).isBaby();
        }
        return false;
    }
    
    public static String getIceFireDragonGender(LivingEntity entity) {
        try {
            CompoundTag nbt = new CompoundTag();
            entity.saveWithoutId(nbt);
            if (nbt.contains("Gender")) {
                boolean isMale = nbt.getBoolean("Gender");
                return isMale ? "male" : "female";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (entity instanceof EntityDragonBase dragon) {
            return dragon.isMale() ? "male" : "female";
        }
        return null;
    }
    
    public static void syncIceFireDragonGender(LivingEntity entity) {
        if (!isIceFireDragon(entity)) return;
        String correctGender = getIceFireDragonGender(entity);
        if (correctGender != null) {
            String currentGender = GenderGameplayEvents.getGender(entity);
            if (currentGender == null || !currentGender.equals(correctGender)) {
                GenderGameplayEvents.setGender(entity, correctGender);
                GenderGameplayEvents.setSterile(entity, false);
            }
        }
    }
    
    public static boolean canIceFireDragonMate(Animal self, Animal partner) {
        String genderSelf = getIceFireDragonGender(self);
        String genderPartner = getIceFireDragonGender(partner);
        
        if (genderSelf == null || genderPartner == null) return true;
        
        if (genderSelf.equals(genderPartner)) {
            boolean isMalePair = genderSelf.equals("male");
            if ((isMalePair && !GenderConfig.isAllowMaleMaleBreed()) ||
                (!isMalePair && !GenderConfig.isAllowFemaleFemaleBreed())) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean isExternalMob(LivingEntity entity) {
        return isNaturalistLion(entity) || isPrimalLion(entity) || isIceFireDragon(entity);
    }
    
    public static String getExternalMobId(LivingEntity entity) {
        if (isNaturalistLion(entity)) return "naturalist:lion";
        if (isPrimalLion(entity)) return "primal:lion";
        if (isIceFireDragon(entity)) {
            ResourceLocation regName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            return regName != null ? regName.toString() : null;
        }
        return null;
    }
    
    public static void assignGenderIfMissing(LivingEntity entity) {
        if (isNaturalistLion(entity)) {
            String correctGender = determineNaturalistLionGender(entity);
            if (correctGender != null && GenderGameplayEvents.getGender(entity) == null) {
                GenderGameplayEvents.setGender(entity, correctGender);
                boolean sterile = GenderGameplayEvents.shouldBeSterile(entity.getRandom());
                GenderGameplayEvents.setSterile(entity, sterile);
            }
        } else if (isPrimalLion(entity)) {
            if (GenderGameplayEvents.getGender(entity) == null) {
                String correctGender = determinePrimalLionGender(entity);
                if (correctGender != null) {
                    GenderGameplayEvents.setGender(entity, correctGender);
                    boolean sterile = GenderGameplayEvents.shouldBeSterile(entity.getRandom());
                    GenderGameplayEvents.setSterile(entity, sterile);
                }
            }
        } else if (isIceFireDragon(entity)) {
            String correctGender = getIceFireDragonGender(entity);
            if (correctGender != null && GenderGameplayEvents.getGender(entity) == null) {
                GenderGameplayEvents.setGender(entity, correctGender);
                GenderGameplayEvents.setSterile(entity, false);
            }
        }
    }
    
    public static void handleExternalMobScanner(ServerPlayer serverPlayer, LivingEntity entity, String mobId) {
        if (isIceFireDragon(entity)) {
            String gender = getIceFireDragonGender(entity);
            if (gender != null) {
                GenderGameplayEvents.setGender(entity, gender);
                GenderGameplayEvents.setSterile(entity, false);
                sendGenderMessage(serverPlayer, entity.getUUID(), mobId, gender, false);
            }
            return;
        }
        
        if (isNaturalistLion(entity)) {
            if (isNaturalistLionBaby(entity)) {
                serverPlayer.displayClientMessage(Component.translatable("genderbub.gender.baby"), true);
                return;
            }
            
            String correctGender = determineNaturalistLionGender(entity);
            if (correctGender == null) return;
            
            String currentGender = GenderGameplayEvents.getGender(entity);
            
            if (currentGender == null) {
                GenderGameplayEvents.setGender(entity, correctGender);
                boolean sterile = GenderGameplayEvents.shouldBeSterile(entity.getRandom());
                GenderGameplayEvents.setSterile(entity, sterile);
            }
            
            String gender = GenderGameplayEvents.getGender(entity);
            boolean sterile = GenderGameplayEvents.isSterile(entity);
            
            if (gender != null) {
                sendGenderMessage(serverPlayer, entity.getUUID(), mobId, gender, sterile);
            }
        } else if (isPrimalLion(entity)) {
            if (isPrimalLionBaby(entity)) {
                serverPlayer.displayClientMessage(Component.translatable("genderbub.gender.baby"), true);
                return;
            }
            
            String correctGender = determinePrimalLionGender(entity);
            if (correctGender == null) return;
            
            String currentGender = GenderGameplayEvents.getGender(entity);
            
            if (currentGender == null) {
                GenderGameplayEvents.setGender(entity, correctGender);
                boolean sterile = GenderGameplayEvents.shouldBeSterile(entity.getRandom());
                GenderGameplayEvents.setSterile(entity, sterile);
            } else if (!correctGender.equals(currentGender)) {
                GenderGameplayEvents.setGender(entity, correctGender);
            }
            
            String gender = GenderGameplayEvents.getGender(entity);
            boolean sterile = GenderGameplayEvents.isSterile(entity);
            
            if (gender != null) {
                sendGenderMessage(serverPlayer, entity.getUUID(), mobId, gender, sterile);
            }
        }
    }
    
    private static void sendGenderMessage(ServerPlayer player, java.util.UUID entityId, String mobId, String gender, boolean sterile) {
        com.bebub.genderbub.network.NetworkHandler.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
            new com.bebub.genderbub.network.GenderSyncPacket(entityId, mobId, gender, sterile)
        );
        
        int color;
        if (sterile) {
            color = 0xAAAAAA;
        } else if (gender.equals("male")) {
            color = 0x55AAFF;
        } else {
            color = 0xFF55FF;
        }
        
        String key = sterile ? (gender.equals("male") ? "genderbub.gender.sterile.male" : "genderbub.gender.sterile.female") 
                            : (gender.equals("male") ? "genderbub.gender.male" : "genderbub.gender.female");
        Component displayComponent = Component.translatable(key).withStyle(style -> style.withColor(color));
        player.displayClientMessage(displayComponent, true);
    }
}