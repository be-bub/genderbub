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

    private static final int CHECK_THRESHOLD = 1000;
    private static final int BLOCKED_EGG_TIME = 72000;

    @Unique
    private int checkCooldown = 0;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        Animal animal = (Animal)(Object)this;
        if (animal.level().isClientSide()) return;
        if (animal.isBaby()) return;

        if (checkCooldown > 0) {
            checkCooldown--;
            return;
        }
        checkCooldown = 20;

        CompoundTag nbt = new CompoundTag();
        animal.saveWithoutId(nbt);

        if (!nbt.contains("EggLayTime")) return;

        int eggTime = nbt.getInt("EggLayTime");
        if (eggTime == -1 || eggTime > CHECK_THRESHOLD) return;

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType());
        if (id == null) return;

        String mobIdStr = id.toString();
        if (!GenderConfig.getEnabledMobs().contains(mobIdStr)) return;

        String gender = GenderCore.getGender(animal);
        if (gender.equals("none")) return;

        if (GenderConfig.isEggLayingBlocked(mobIdStr, gender, GenderCore.isSterile(animal))) {
            nbt.putInt("EggLayTime", BLOCKED_EGG_TIME);
            animal.load(nbt);
        }
    }
}