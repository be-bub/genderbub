package com.bebub.genderbub.mixins;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Villager.class)
public class VillagerMixin {

    @Inject(method = "canBreed", at = @At("RETURN"), cancellable = true)
    private void onCanBreed(CallbackInfoReturnable<Boolean> cir) {
        Villager v = (Villager)(Object)this;
        if (!GenderConfig.isEnableVillagers()) return;
        if (!cir.getReturnValue()) return;

        String g = GenderCore.getGender(v);
        boolean s = GenderCore.isSterile(v);

        if (s && !GenderConfig.isAllowSterileBreed()) {
            cir.setReturnValue(false);
            applyCooldown(v);
            spawnParticles(v);
            return;
        }

        if (g.equals("none")) return;

        Optional<AgeableMob> partnerOpt = v.getBrain().getMemory(MemoryModuleType.BREED_TARGET);
        if (!partnerOpt.isPresent()) return;
        if (!(partnerOpt.get() instanceof Villager partner)) return;

        String pg = GenderCore.getGender(partner);
        boolean ps = GenderCore.isSterile(partner);

        if (ps && !GenderConfig.isAllowSterileBreed()) {
            cir.setReturnValue(false);
            applyCooldown(v);
            applyCooldown(partner);
            spawnParticles(v);
            spawnParticles(partner);
            return;
        }

        if (g.equals(pg)) {
            if ((g.equals("male") && !GenderConfig.isAllowMaleMaleBreed()) ||
                (g.equals("female") && !GenderConfig.isAllowFemaleFemaleBreed())) {
                cir.setReturnValue(false);
                applyCooldown(v);
                applyCooldown(partner);
                spawnParticles(v);
                spawnParticles(partner);
            }
        }
    }

    private void applyCooldown(Villager v) {
        v.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
        v.setAge(6000);
        v.getInventory().removeAllItems();
    }

    private void spawnParticles(Villager v) {
        if (v.level() instanceof ServerLevel l) {
            for (int i = 0; i < 6; i++) {
                Vec3 pos = v.position().add(0, 1.2, 0).add(l.random.nextGaussian() * 0.5, 0, l.random.nextGaussian() * 0.5);
                l.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            }
        }
    }
}