package com.bebub.genderbub.mixins;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.compat.GenderAddon;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public class BreedMixin {
    
    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true)
    private void onCanMate(Animal other, CallbackInfoReturnable<Boolean> cir) {
        Animal self = (Animal)(Object)this;
        if (self.level().isClientSide()) return;
        
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(self.getType());
        if (id != null && GenderConfig.getGenderOnlyMobs().contains(id.toString())) {
            return;
        }
        
        if (GenderAddon.isIceFireDragon(self) || GenderAddon.isIceFireDragon(other)) {
            if (!GenderAddon.canIceFireDragonMate(self, other)) {
                cir.setReturnValue(false);
            }
            return;
        }
        
        String g1 = GenderCore.getGender(self);
        String g2 = GenderCore.getGender(other);
        if (g1.equals("none") || g2.equals("none")) return;
        
        if (g1.equals(g2)) {
            boolean male = g1.equals("male");
            if ((male && !GenderConfig.isAllowMaleMaleBreed()) || (!male && !GenderConfig.isAllowFemaleFemaleBreed())) {
                cir.setReturnValue(false);
                return;
            }
        }
        
        if ((GenderCore.isSterile(self) || GenderCore.isSterile(other)) && !GenderConfig.isAllowSterileBreed()) {
            cir.setReturnValue(false);
        }
    }
}