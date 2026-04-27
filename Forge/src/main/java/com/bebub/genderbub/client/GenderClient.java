package com.bebub.genderbub.client;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.GenderMod;
import com.bebub.genderbub.config.GenderConfig;
import com.bebub.genderbub.network.GenderNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = GenderMod.MOD_ID, value = Dist.CLIENT)
public class GenderClient {

    private static boolean jadePresent = false;
    private static boolean jadeChecked = false;

    public static void init() {
        jadePresent = ModList.get().isLoaded("jade");
        jadeChecked = true;
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(GenderClient::init);
    }

    public static boolean isScanner(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.toString().equals("genderbub:magnifying_glass");
    }

    private static boolean isJadePresent() {
        if (!jadeChecked) {
            jadePresent = ModList.get().isLoaded("jade");
            jadeChecked = true;
        }
        return jadePresent;
    }

    private static boolean shouldShowGender(LivingEntity entity, Player player) {
        if (GenderCore.isGenderCached(entity)) return false;
        String gender = GenderCore.getGender(entity);
        if (gender.equals("none")) return false;
        if (isJadePresent() && GenderConfig.isHideWithJade()) return false;
        if (GenderConfig.isRequireScanner()) {
            boolean hasScanner = isScanner(player.getMainHandItem()) || isScanner(player.getOffhandItem());
            if (!hasScanner) return false;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.hitResult instanceof EntityHitResult && ((EntityHitResult) mc.hitResult).getEntity() == entity;
    }

    private static LivingEntity getTargetEntity(Minecraft mc) {
        if (mc.crosshairPickEntity instanceof LivingEntity living) {
            return living;
        }
        if (mc.hitResult instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || mc.screen != null || mc.options.hideGui) return;
        LivingEntity target = getTargetEntity(mc);
        if (target == null) return;
        if (!shouldShowGender(target, player)) return;
        GenderUI.renderGenderInfo(event.getGuiGraphics(), target, mc);
    }

    private static boolean shouldCancelInteractionLocal(LivingEntity entity, ItemStack stack) {
        String gender = GenderCore.getGender(entity);
        if (gender.equals("none")) return false;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return false;
        return GenderConfig.isItemBlocked(id.toString(), gender, GenderCore.isSterile(entity), stack.getItem());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof LivingEntity entity)) return;
        if (!entity.level().isClientSide()) return;
        if (event.getItemStack().isEmpty()) return;

        ItemStack stack = event.getItemStack();

        if (isScanner(stack)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (GenderCore.isGenderCached(entity)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        String gender = GenderCore.getGender(entity);
        if (gender.equals("none")) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        if (GenderConfig.isSyncConfigRules()) {
            boolean isOffhand = event.getHand() == net.minecraft.world.InteractionHand.OFF_HAND;
            GenderNetwork.sendInteraction(entity.getId(), isOffhand);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        } else {
            if (shouldCancelInteractionLocal(entity, stack)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        }
    }
}