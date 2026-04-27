package com.bebub.genderbub.item;

import com.bebub.genderbub.GenderCore;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GenderSticksItem extends Item {

    private final StickType type;

    public enum StickType {
        GENDER,
        STERILE
    }

    public GenderSticksItem(StickType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, net.minecraft.world.InteractionHand hand) {
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;

        String currentGender = GenderCore.getGender(target);
        if (currentGender.equals("none")) {
            return InteractionResult.SUCCESS;
        }

        switch (type) {
            case GENDER:
                if (currentGender.equals("male")) {
                    GenderCore.setGender(target, "female");
                } else {
                    GenderCore.setGender(target, "male");
                }
                GenderCore.setSterile(target, false);
                break;
            case STERILE:
                boolean currentSterile = GenderCore.isSterile(target);
                GenderCore.setSterile(target, !currentSterile);
                break;
        }
        return InteractionResult.SUCCESS;
    }
}