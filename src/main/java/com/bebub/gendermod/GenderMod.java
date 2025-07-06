package com.bebub.gendermod;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import com.bebub.gendermod.GenderConfig;

@Mod("bub_addition")
public class GenderMod {
    public GenderMod() {
        GenderConfig.register();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new GenderEvents());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GenderEvents.setConfig(
                GenderConfig.ENABLED_MOBS.get().stream().toList(),
                GenderConfig.GENDER_RULES.get().stream().toList()
            );
        });
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Animal animal) {
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(animal.getType());
            if (entityId != null && GenderConfig.ENABLED_MOBS.get().contains(entityId.toString())) {
                animal.setPersistenceRequired();
            }
        }
    }
}