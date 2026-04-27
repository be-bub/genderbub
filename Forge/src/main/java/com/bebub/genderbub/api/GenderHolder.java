package com.bebub.genderbub.api;

import net.minecraft.world.entity.LivingEntity;

public interface GenderHolder {
    String getGender();
    void setGender(String gender);
    boolean isSterile();
    void setSterile(boolean sterile);
}