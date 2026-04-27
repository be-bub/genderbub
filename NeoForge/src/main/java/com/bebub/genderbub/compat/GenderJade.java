package com.bebub.genderbub.compat;

import com.bebub.genderbub.GenderCore;
import com.bebub.genderbub.util.GenderDisplayUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class GenderJade implements IWailaPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("genderbub", "gender_info");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(new GenderProvider(), LivingEntity.class);
        registration.markAsClientFeature(ID);
    }

    public static class GenderProvider implements IEntityComponentProvider {
        
        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (!(accessor.getEntity() instanceof LivingEntity living)) return;
            
            String gender = GenderCore.getGender(living);
            if (gender.equals("none")) return;
            
            Component component = GenderDisplayUtil.getGenderComponent(living);
            if (component != null) {
                tooltip.add(component);
            }
        }
        
        @Override
        public ResourceLocation getUid() { return ID; }
    }
}