package com.bebub.genderbub.mixins;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.api.GenderHolder;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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

    public DataMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Unique
    private LivingEntity asLiving() {
        return (LivingEntity) (Object) this;
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

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    protected void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType());
        boolean isEnabled = isMobEnabledForData(mobId);
        String currentGender = GenderCore.getGender(asLiving());
        boolean currentSterile = GenderCore.isSterile(asLiving());

        if (!isEnabled && !currentGender.equals("none") && !currentGender.equals("baby") && !currentGender.startsWith("cached_")) {
            tag.putString("bubgender", "cached_" + currentGender);
            tag.putBoolean("bubsterile", currentSterile);
            GenderCore.setGender(asLiving(), "cached_" + currentGender);
        } else if (isEnabled && !currentGender.equals("none") && !currentGender.equals("baby") && !currentGender.startsWith("cached_")) {
            tag.putString("bubgender", currentGender);
            tag.putBoolean("bubsterile", currentSterile);
        } else if (isEnabled && currentGender.equals("baby")) {
            tag.putString("bubgender", "baby");
            tag.putBoolean("bubsterile", false);
        } else if (isEnabled && currentGender.startsWith("cached_")) {
            String realGender = currentGender.substring(7);
            if (realGender.equals("baby")) {
                tag.putString("bubgender", "baby");
            } else {
                tag.putString("bubgender", realGender);
            }
            tag.putBoolean("bubsterile", currentSterile);
            GenderCore.setGender(asLiving(), realGender);
        } else if (!isEnabled && currentGender.startsWith("cached_")) {
            tag.putString("bubgender", currentGender);
            tag.putBoolean("bubsterile", currentSterile);
        } else if (!isEnabled && currentGender.equals("baby")) {
            tag.putString("bubgender", "cached_baby");
            tag.putBoolean("bubsterile", false);
            GenderCore.setGender(asLiving(), "cached_baby");
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    protected void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        CompoundTag persistentData = getPersistentData();

        if (persistentData.contains("GenderMod_Gender")) {
            String oldGender = persistentData.getString("GenderMod_Gender");
            boolean oldSterile = persistentData.getBoolean("GenderMod_Sterile");
            GenderCore.setGender(asLiving(), oldGender);
            GenderCore.setSterile(asLiving(), oldSterile);
            persistentData.remove("GenderMod_Gender");
            persistentData.remove("GenderMod_Sterile");
            return;
        }

        if (tag.contains("GenderMod_Gender")) {
            String oldGender = tag.getString("GenderMod_Gender");
            boolean oldSterile = tag.getBoolean("GenderMod_Sterile");
            GenderCore.setGender(asLiving(), oldGender);
            GenderCore.setSterile(asLiving(), oldSterile);
            tag.remove("GenderMod_Gender");
            tag.remove("GenderMod_Sterile");
            return;
        }

        if (tag.contains("ForgeData")) {
            CompoundTag forgeData = tag.getCompound("ForgeData");
            if (forgeData.contains("GenderMod_Gender")) {
                String oldGender = forgeData.getString("GenderMod_Gender");
                boolean oldSterile = forgeData.getBoolean("GenderMod_Sterile");
                GenderCore.setGender(asLiving(), oldGender);
                GenderCore.setSterile(asLiving(), oldSterile);
                forgeData.remove("GenderMod_Gender");
                forgeData.remove("GenderMod_Sterile");
                return;
            }
        }

        if (tag.contains("bubgender")) {
            String gender = tag.getString("bubgender");
            boolean sterile = tag.getBoolean("bubsterile");
            
            if (gender.startsWith("cached_")) {
                String realGender = gender.substring(7);
                GenderCore.setGender(asLiving(), realGender);
            } else {
                GenderCore.setGender(asLiving(), gender);
            }
            GenderCore.setSterile(asLiving(), sterile);
        }
    }

    @Override
    public String getGender() {
        return GenderCore.getGender(asLiving());
    }

    @Override
    public void setGender(String g) {
        GenderCore.setGender(asLiving(), g);
    }

    @Override
    public boolean isSterile() {
        return GenderCore.isSterile(asLiving());
    }

    @Override
    public void setSterile(boolean s) {
        GenderCore.setSterile(asLiving(), s);
    }
}