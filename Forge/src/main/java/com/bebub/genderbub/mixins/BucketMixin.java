package com.bebub.genderbub.mixins;

import com.bebub.genderbub.GenderCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Axolotl.class)
public class BucketMixin {
    
    @Inject(method = "saveToBucketTag", at = @At("RETURN"))
    private void onSaveToBucketTag(ItemStack bucket, CallbackInfo ci) {
        Axolotl entity = (Axolotl) (Object) this;
        CompoundTag tag = bucket.getOrCreateTag();
        String gender = GenderCore.getGender(entity);
        boolean sterile = GenderCore.isSterile(entity);
        
        if (!gender.equals("none")) {
            tag.putString("BucketGender", gender);
            tag.putBoolean("BucketSterile", sterile);
        }
    }
    
    @Inject(method = "loadFromBucketTag", at = @At("RETURN"))
    private static void onLoadFromBucketTag(CompoundTag tag, Axolotl entity, CallbackInfo ci) {
        if (tag.contains("BucketGender")) {
            String gender = tag.getString("BucketGender");
            boolean sterile = tag.getBoolean("BucketSterile");
            
            GenderCore.setGender(entity, gender);
            GenderCore.setSterile(entity, sterile);
        }
    }
}