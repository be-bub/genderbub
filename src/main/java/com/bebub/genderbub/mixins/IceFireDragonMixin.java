package com.bebub.genderbub.mixins;

import com.bebub.genderbub.GenderGameplayEvents;
import com.bebub.genderbub.config.GenderConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityDragonBase.class)
public class IceFireDragonMixin {

    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true)
    private void onCanMate(Animal partner, CallbackInfoReturnable<Boolean> cir) {
        EntityDragonBase self = (EntityDragonBase) (Object) this;

        if (self.level().isClientSide()) return;
        
        ResourceLocation idSelf = ForgeRegistries.ENTITY_TYPES.getKey(self.getType());
        ResourceLocation idPartner = ForgeRegistries.ENTITY_TYPES.getKey(partner.getType());
        String mobIdSelf = idSelf != null ? idSelf.toString() : "";
        String mobIdPartner = idPartner != null ? idPartner.toString() : "";
        
        if (GenderConfig.getGenderOnlyMobs().contains(mobIdSelf) || 
            GenderConfig.getGenderOnlyMobs().contains(mobIdPartner)) {
            return;
        }

        String genderSelf = GenderGameplayEvents.getGender(self);
        String genderPartner = GenderGameplayEvents.getGender(partner);
        boolean sterileSelf = GenderGameplayEvents.isSterile(self);
        boolean sterilePartner = GenderGameplayEvents.isSterile(partner);

        if ((sterileSelf || sterilePartner) && !GenderConfig.isAllowSterileBreed()) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        if (genderSelf != null && genderPartner != null && genderSelf.equals(genderPartner)) {
            boolean isMalePair = genderSelf.equals("male");
            if ((isMalePair && !GenderConfig.isAllowMaleMaleBreed()) ||
                (!isMalePair && !GenderConfig.isAllowFemaleFemaleBreed())) {
                cir.setReturnValue(false);
                cir.cancel();
                return;
            }
        }
    }
}