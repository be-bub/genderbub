package com.bebub.genderbub;

import com.bebub.genderbub.config.GenderConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = GenderMod.MOD_ID)
public class GenderConversion {

    private static class PendingData {
        final CompoundTag data;
        final long timestamp;
        
        PendingData(CompoundTag data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    private static final ConcurrentHashMap<UUID, PendingData> PENDING_GENDER = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onConversionPre(LivingConversionEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!GenderConfig.isKeepVillagerGender()) return;

        Entity entity = event.getEntity();

        if ((entity instanceof Villager || entity instanceof ZombieVillager) && entity instanceof LivingEntity living) {
            String gender = GenderCore.getGender(living);
            boolean sterile = GenderCore.isSterile(living);
            
            if (!gender.equals("none")) {
                CompoundTag data = new CompoundTag();
                data.putString("bubgender", gender);
                data.putBoolean("bubsterile", sterile);
                PENDING_GENDER.put(entity.getUUID(), new PendingData(data));
            }
        }
        
        cleanupStaleEntries();
    }

    @SubscribeEvent
    public static void onConversionPost(LivingConversionEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!GenderConfig.isKeepVillagerGender()) return;

        Entity original = event.getEntity();
        Entity outcome = event.getOutcome();

        if ((outcome instanceof Villager || outcome instanceof ZombieVillager) && outcome instanceof LivingEntity living) {
            PendingData pending = PENDING_GENDER.remove(original.getUUID());

            if (pending != null) {
                CompoundTag data = pending.data;
                GenderCore.setGender(living, data.getString("bubgender"));
                GenderCore.setSterile(living, data.getBoolean("bubsterile"));
            }
        }
    }
    
    private static void cleanupStaleEntries() {
        long now = System.currentTimeMillis();
        long timeout = 2000;
        
        PENDING_GENDER.entrySet().removeIf(entry -> 
            now - entry.getValue().timestamp > timeout
        );
    }
}