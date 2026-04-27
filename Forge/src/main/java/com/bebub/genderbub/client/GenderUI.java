package com.bebub.genderbub.client;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.util.GenderDisplayUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class GenderUI {
    
    private static final int TEXT_Y_OFFSET = 65;
    private static final int TEXT_BG_PADDING_LEFT = 6;
    private static final int TEXT_BG_PADDING_RIGHT = 6;
    private static final int ICON_BG_PADDING_LEFT = 2;
    private static final int ICON_BG_PADDING_RIGHT = 2;
    private static final int BG_PADDING_TOP = 2;
    private static final int BG_PADDING_BOTTOM = 2;
    private static final int ICON_SIZE = 12;
    private static final int TEXT_ICON_GAP = 1;
    
    private static final int BACKGROUND_COLOR = 0x88000000;
    private static final int BORDER_COLOR = 0x33000000;
    
    private static final ResourceLocation MALE_ICON = ResourceLocation.tryParse("genderbub:textures/gui/male.png");
    private static final ResourceLocation FEMALE_ICON = ResourceLocation.tryParse("genderbub:textures/gui/female.png");
    
    public static void renderGenderInfo(GuiGraphics gui, LivingEntity target, Minecraft mc) {
        String gender = GenderCore.getGender(target);
        if (gender.equals("none")) return;
        
        int genderColor = GenderDisplayUtil.getColor(target);
        String genderText = GenderDisplayUtil.getTranslationKey(target);
        String localizedGender = net.minecraft.network.chat.Component.translatable(genderText).getString();
        
        ResourceLocation icon = getGenderIcon(target);
        
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        int textWidth = mc.font.width(localizedGender);
        int bgHeight = ICON_SIZE + BG_PADDING_TOP + BG_PADDING_BOTTOM;
        int bgY = screenHeight - TEXT_Y_OFFSET;
        
        if (icon != null) {
            int textBgWidth = textWidth + TEXT_BG_PADDING_LEFT + TEXT_BG_PADDING_RIGHT;
            int iconBgWidth = ICON_SIZE + ICON_BG_PADDING_LEFT + ICON_BG_PADDING_RIGHT;
            int totalWidth = textBgWidth + TEXT_ICON_GAP + iconBgWidth;
            int textBgX = (screenWidth - totalWidth) / 2;
            int iconBgX = textBgX + textBgWidth + TEXT_ICON_GAP;
            
            renderBackground(gui, textBgX, bgY, textBgWidth, bgHeight);
            renderBackground(gui, iconBgX, bgY, iconBgWidth, bgHeight);
            
            int centerY = bgY + bgHeight / 2;
            int textY = centerY - mc.font.lineHeight / 2;
            int textX = textBgX + TEXT_BG_PADDING_LEFT;
            int iconY = centerY - ICON_SIZE / 2;
            int iconX = iconBgX + ICON_BG_PADDING_LEFT;
            
            gui.drawString(mc.font, localizedGender, textX, textY, genderColor);
            renderIcon(gui, icon, iconX, iconY, genderColor);
        } else {
            int textBgWidth = textWidth + TEXT_BG_PADDING_LEFT + TEXT_BG_PADDING_RIGHT;
            int textBgX = (screenWidth - textBgWidth) / 2;
            
            renderBackground(gui, textBgX, bgY, textBgWidth, bgHeight);
            
            int centerY = bgY + bgHeight / 2;
            int textY = centerY - mc.font.lineHeight / 2;
            int textX = textBgX + TEXT_BG_PADDING_LEFT;
            
            gui.drawString(mc.font, localizedGender, textX, textY, genderColor);
        }
    }
    
    private static void renderBackground(GuiGraphics gui, int x, int y, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        gui.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
        
        gui.fill(x, y, x + width, y + 1, BORDER_COLOR);
        gui.fill(x, y + height - 1, x + width, y + height, BORDER_COLOR);
        gui.fill(x, y, x + 1, y + height, BORDER_COLOR);
        gui.fill(x + width - 1, y, x + width, y + height, BORDER_COLOR);
    }
    
    private static void renderIcon(GuiGraphics gui, ResourceLocation icon, int x, int y, int color) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, icon);
        RenderSystem.setShaderColor(
            ((color >> 16) & 0xFF) / 255.0f,
            ((color >> 8) & 0xFF) / 255.0f,
            (color & 0xFF) / 255.0f,
            1.0f
        );
        
        gui.blit(icon, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
    
    private static ResourceLocation getGenderIcon(LivingEntity entity) {
        String gender = GenderCore.getGender(entity);
        if (gender.equals("male")) return MALE_ICON;
        if (gender.equals("female")) return FEMALE_ICON;
        return null;
    }
}