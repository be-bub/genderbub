package com.bebub.genderbub.mixins;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class EggMixin {

    private static final int BLOCKED_EGG_TIME = 72000;
    private static final int DANGER_THRESHOLD = 6000;

    @Unique
    private int checkCooldown = 0;
    
    @Unique
    private String cachedMobId = null;
    
    @Unique
    private Boolean cachedShouldBlock = null;
    
    @Unique
    private int cachedEggTime = -1;
    
    @Unique
    private int cachedEggTimeTick = 0;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        Animal animal = (Animal)(Object)this;
        
        if (animal.level().isClientSide()) return;
        if (animal.isBaby()) return;

        int currentTick = animal.tickCount;
        
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        if (id == null) return;
        
        String mobIdStr = id.toString();
        
        boolean isNewMob = !mobIdStr.equals(cachedMobId);
        
        if (isNewMob) {
            cachedMobId = mobIdStr;
            cachedShouldBlock = null;
            cachedEggTime = -1;
            checkCooldown = 0;
        }
        
        if (checkCooldown > 0) {
            checkCooldown--;
            return;
        }
        
        if (cachedShouldBlock == null) {
            String gender = GenderCore.getGender(animal);
            if (gender.equals("none") || gender.startsWith("cached_")) {
                cachedShouldBlock = false;
                return;
            }
            boolean sterile = GenderCore.isSterile(animal);
            cachedShouldBlock = GenderConfig.isEggLayingBlocked(mobIdStr, gender, sterile);
        }
        
        if (!cachedShouldBlock) return;
        
        if (cachedEggTime != -1 && !isNewMob) {
            int ticksPassed = currentTick - cachedEggTimeTick;
            int estimatedCurrent = cachedEggTime - ticksPassed;
            
            if (estimatedCurrent > DANGER_THRESHOLD) {
                checkCooldown = estimatedCurrent - DANGER_THRESHOLD;
                return;
            }
        }
        
        CompoundTag nbt = new CompoundTag();
        animal.saveWithoutId(nbt);
        
        if (!nbt.contains("EggLayTime")) return;
        
        int eggTime = nbt.getInt("EggLayTime");
        
        cachedEggTime = eggTime;
        cachedEggTimeTick = currentTick;
        
        if (eggTime == -1 || eggTime <= DANGER_THRESHOLD) {
            nbt.putInt("EggLayTime", BLOCKED_EGG_TIME);
            animal.load(nbt);
            cachedEggTime = BLOCKED_EGG_TIME;
            cachedEggTimeTick = currentTick;
            checkCooldown = BLOCKED_EGG_TIME - DANGER_THRESHOLD;
        } else {
            int remaining = eggTime - DANGER_THRESHOLD;
            checkCooldown = remaining > 0 ? remaining : 100;
        }
    }
}