package com.bebub.genderbub;

import com.bebub.genderbub.config.GenderConfig;
import com.bebub.genderbub.item.GenderScannerItem;
import com.bebub.genderbub.item.GenderSticksItem;
import com.bebub.genderbub.network.GenderNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GenderMod.MOD_ID)
public class GenderMod {
    public static final String MOD_ID = "genderbub";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    
    public static final RegistryObject<Item> GENDER_SCANNER = ITEMS.register("magnifying_glass", GenderScannerItem::new);
    public static final RegistryObject<Item> GENDER_STICK = ITEMS.register("gender_stick", () -> new GenderSticksItem(GenderSticksItem.StickType.GENDER));
    public static final RegistryObject<Item> STERILE_STICK = ITEMS.register("sterile_stick", () -> new GenderSticksItem(GenderSticksItem.StickType.STERILE));
    
    public static final RegistryObject<CreativeModeTab> GENDER_TAB = CREATIVE_TABS.register("gender_tab", 
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.genderbub"))
            .icon(() -> new ItemStack(GENDER_SCANNER.get()))
            .displayItems((parameters, output) -> {
                output.accept(GENDER_SCANNER.get());
                output.accept(GENDER_STICK.get());
                output.accept(STERILE_STICK.get());
            })
            .build());

    public GenderMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        CREATIVE_TABS.register(modBus);
        
        GenderConfig.init();
        GenderCore.init();
        GenderNetwork.init();
        
        MinecraftForge.EVENT_BUS.register(GenderEvents.class);
        MinecraftForge.EVENT_BUS.register(GenderConversion.class);
        
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GenderConfig.performFirstScanIfNeeded();
            LOGGER.info("GenderBub loaded");
        });
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GenderCore.initClient();
        });
    }
}