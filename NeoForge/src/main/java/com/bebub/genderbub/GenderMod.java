package com.bebub.genderbub;

import com.bebub.genderbub.command.ServerCommands;
import com.bebub.genderbub.config.GenderConfig;
import com.bebub.genderbub.item.GenderScannerItem;
import com.bebub.genderbub.item.GenderSticksItem;
import com.bebub.genderbub.network.GenderNetwork;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GenderMod.MOD_ID)
public class GenderMod {
    public static final String MOD_ID = "genderbub";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> GENDER = ATTACHMENT_TYPES.register(
        "gender", () -> AttachmentType.<String>builder(() -> "none")
            .serialize(Codec.STRING)
            .sync((value, oldValue) -> true, ByteBufCodecs.STRING_UTF8)
            .copyOnDeath()
            .build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> STERILE = ATTACHMENT_TYPES.register(
        "sterile", () -> AttachmentType.<Boolean>builder(() -> false)
            .serialize(Codec.BOOL)
            .sync((value, oldValue) -> true, ByteBufCodecs.BOOL)
            .copyOnDeath()
            .build()
    );

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<Item, GenderScannerItem> GENDER_SCANNER = ITEMS.register("magnifying_glass", () -> new GenderScannerItem(new Item.Properties()));
    public static final DeferredHolder<Item, GenderSticksItem> GENDER_STICK = ITEMS.register("gender_stick", () -> new GenderSticksItem(GenderSticksItem.StickType.GENDER, new Item.Properties()));
    public static final DeferredHolder<Item, GenderSticksItem> STERILE_STICK = ITEMS.register("sterile_stick", () -> new GenderSticksItem(GenderSticksItem.StickType.STERILE, new Item.Properties()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GENDER_TAB = CREATIVE_TABS.register("gender_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.genderbub"))
            .icon(() -> new ItemStack(GENDER_SCANNER.get()))
            .displayItems((parameters, output) -> {
                output.accept(GENDER_SCANNER.get());
                output.accept(GENDER_STICK.get());
                output.accept(STERILE_STICK.get());
            })
            .build());

    public GenderMod(IEventBus modBus, ModContainer modContainer) {
        ATTACHMENT_TYPES.register(modBus);
        ITEMS.register(modBus);
        CREATIVE_TABS.register(modBus);

        GenderConfig.init();
        GenderCore.init();

        NeoForge.EVENT_BUS.register(GenderEvents.class);
        NeoForge.EVENT_BUS.register(GenderConversion.class);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerPayloads);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            GenderConfig.performFirstScanIfNeeded();
            LOGGER.info("GenderBub loaded");
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(GenderCore::initClient);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ServerCommands.register(event.getDispatcher());
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        GenderNetwork.init(event);
    }
}