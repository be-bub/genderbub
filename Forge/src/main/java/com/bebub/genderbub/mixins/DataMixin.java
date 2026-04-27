package com.bebub.genderbub.mixins;

import com.bebub.genderbub.api.GenderHolder;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class DataMixin extends Entity implements GenderHolder {

    @Unique
    private static final EntityDataAccessor<String> GENDER = 
        SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.STRING);
    @Unique
    private static final EntityDataAccessor<Boolean> STERILE = 
        SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

    public DataMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Unique
    private boolean isMobEnabledForData(ResourceLocation mobId) {
        if (mobId == null) return false;
        String id = mobId.toString();
        if (id.equals("minecraft:villager") || id.equals("minecraft:zombie_villager")) {
            return GenderConfig.isEnableVillagers();
        }
        return GenderConfig.isMobEnabled(id);
    }

    @Unique
    private boolean shouldCache(boolean isEnabled, String currentGender) {
        if (isEnabled) return false;
        if (currentGender.equals("none")) return false;
        if (currentGender.startsWith("cached_")) return false;
        return true;
    }

    @Unique
    private boolean shouldRestore(boolean isEnabled, String currentGender) {
        if (!isEnabled) return false;
        if (!currentGender.startsWith("cached_")) return false;
        return true;
    }

    @Inject(method = "defineSynchedData", at = @At("RETURN"))
    protected void defineSynchedData(CallbackInfo ci) {
        this.entityData.define(GENDER, "none");
        this.entityData.define(STERILE, false);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    protected void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("bubgender")) return;

        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType());
        boolean isEnabled = isMobEnabledForData(mobId);
        String currentGender = this.entityData.get(GENDER);
        boolean currentSterile = this.entityData.get(STERILE);

        if (shouldCache(isEnabled, currentGender)) {
            tag.putString("bubgender", "cached_" + currentGender);
            tag.putBoolean("bubsterile", currentSterile);
            this.entityData.set(GENDER, "cached_" + currentGender);
        } else if (shouldRestore(isEnabled, currentGender)) {
            String realGender = currentGender.substring(7);
            tag.putString("bubgender", realGender);
            tag.putBoolean("bubsterile", currentSterile);
            this.entityData.set(GENDER, realGender);
        } else if (isEnabled && !currentGender.equals("none") && !currentGender.startsWith("cached_")) {
            tag.putString("bubgender", currentGender);
            tag.putBoolean("bubsterile", currentSterile);
        } else if (!isEnabled && currentGender.startsWith("cached_")) {
            tag.putString("bubgender", currentGender);
            tag.putBoolean("bubsterile", currentSterile);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    protected void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        CompoundTag persistentData = getPersistentData();

        if (persistentData.contains("GenderMod_Gender")) {
            String oldGender = persistentData.getString("GenderMod_Gender");
            boolean oldSterile = persistentData.getBoolean("GenderMod_Sterile");
            this.entityData.set(GENDER, oldGender);
            this.entityData.set(STERILE, oldSterile);
            persistentData.remove("GenderMod_Gender");
            persistentData.remove("GenderMod_Sterile");
            return;
        }

        if (tag.contains("bubgender")) {
            String gender = tag.getString("bubgender");
            boolean sterile = tag.getBoolean("bubsterile");
            
            if (gender.startsWith("cached_")) {
                String realGender = gender.substring(7);
                this.entityData.set(GENDER, realGender);
            } else {
                this.entityData.set(GENDER, gender);
            }
            this.entityData.set(STERILE, sterile);
        }
    }

    @Override
    public String getGender() {
        String value = this.entityData.get(GENDER);
        if (value.startsWith("cached_")) {
            return "none";
        }
        return value;
    }

    @Override
    public void setGender(String g) {
        if (g == null) g = "none";
        this.entityData.set(GENDER, g);
    }

    @Override
    public boolean isSterile() {
        return this.entityData.get(STERILE);
    }

    @Override
    public void setSterile(boolean s) {
        this.entityData.set(STERILE, s);
    }
}