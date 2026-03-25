package com.bebub.gendermod.client;

import com.bebub.gendermod.GenderMod;
import com.bebub.gendermod.item.GenderScannerItem;
import com.bebub.gendermod.config.Config;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = GenderMod.MOD_ID, value = Dist.CLIENT)
public class ClientGenderHandler {
    private static final TextColor MALE_COLOR = TextColor.parseColor("#55AAFF");
    private static final TextColor FEMALE_COLOR = TextColor.parseColor("#FF55FF");
    private static final int HIDE_DELAY_TICKS = 20;

    private static String lastDisplayedGender = null;
    private static int hideTimer = 0;
    private static int currentRadius = Config.getDisplayRadius();

    @SubscribeEvent
    public static void onRenderLivingPostSymbol(RenderLivingEvent.Post<?, ?> event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Animal animal)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack offhand = mc.player.getOffhandItem();
        if (!(offhand.getItem() instanceof GenderScannerItem)) return;

        currentRadius = Config.getDisplayRadius();
        double distSq = animal.distanceToSqr(mc.player);
        if (distSq > currentRadius * currentRadius) return;

        UUID animalId = animal.getUUID();
        String gender = ClientGenderCache.get(animalId);
        if (gender == null) return;

        String key = gender.equals("male") ? "gendermod.gender.male" : "gendermod.gender.female";
        String text = Component.translatable(key).getString();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        
        float yOffset = animal.getBbHeight() + 0.5f;
        if (animal.hasCustomName()) {
            yOffset += 0.3f;
        }
        
        poseStack.translate(0, yOffset, 0);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Matrix4f matrix = poseStack.last().pose();
        int color = gender.equals("male") ? 0x55AAFF : 0xFF55FF;
        
        float textWidth = mc.font.width(text);
        mc.font.drawInBatch(text, -textWidth / 2f, 0, color, false,
                matrix, event.getMultiBufferSource(), net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);

        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onClientTickLook(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof GenderScannerItem) {
            HitResult hit = mc.hitResult;
            if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
                Entity target = ((EntityHitResult) hit).getEntity();
                if (target instanceof Animal animal) {
                    UUID animalId = animal.getUUID();
                    String gender = ClientGenderCache.get(animalId);
                    if (gender != null) {
                        if (!gender.equals(lastDisplayedGender) || hideTimer > 0) {
                            lastDisplayedGender = gender;
                            hideTimer = HIDE_DELAY_TICKS;
                            MutableComponent genderText = createGenderText(gender);
                            MutableComponent message = Component.translatable("gendermod.message.gender", genderText);
                            player.displayClientMessage(message, true);
                        }
                        return;
                    }
                }
            }
        }
        
        if (hideTimer > 0) {
            hideTimer--;
            if (hideTimer == 0) {
                clearActionBar(player);
            }
        }
    }

    private static void clearActionBar(LocalPlayer player) {
        if (lastDisplayedGender != null) {
            lastDisplayedGender = null;
            player.displayClientMessage(Component.empty(), true);
        }
    }

    private static MutableComponent createGenderText(String gender) {
        TextColor color = gender.equals("male") ? MALE_COLOR : FEMALE_COLOR;
        String key = gender.equals("male") ? "gendermod.gender.male" : "gendermod.gender.female";
        return Component.translatable(key).withStyle(style -> style.withColor(color));
    }
}