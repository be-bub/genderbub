package com.bebub.gendermod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import com.bebub.gendermod.GenderMod;
import com.bebub.gendermod.GenderGameplayEvents;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Mod.EventBusSubscriber(modid = GenderMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static class Server {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> ENABLED_MOBS;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> GENDER_RULES;
        public final ForgeConfigSpec.IntValue DISPLAY_RADIUS;
        public final ForgeConfigSpec.IntValue MALE_CHANCE;

        Server(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            
            ENABLED_MOBS = builder
                .comment("List of mobs that should have genders (example: minecraft:cow)")
                .defineList("enabledMobs", 
                    Arrays.asList("minecraft:cow"),
                    obj -> obj instanceof String);
            
            GENDER_RULES = builder
                .comment("Gender interaction rules in format: mobId;gender;itemId (example: minecraft:cow;male;minecraft:bucket)")
                .defineList("genderRules",
                    Arrays.asList("minecraft:cow;male;minecraft:bucket"),
                    obj -> obj instanceof String);
            
            DISPLAY_RADIUS = builder
                .comment("Radius in blocks to show gender tags above mobs (default: 16)")
                .defineInRange("displayRadius", 16, 5, 100);
            
            MALE_CHANCE = builder
                .comment("Chance for male gender in percent (0-100). 50 = 50% male, 50% female. 70 = 70% male, 30% female")
                .defineInRange("maleChance", 50, 0, 100);
            
            builder.pop();
        }
    }

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static List<String> getEnabledMobs() {
        return new ArrayList<>(SERVER.ENABLED_MOBS.get());
    }

    public static List<GenderRuleConfig> getGenderRules() {
        List<String> rules = new ArrayList<>(SERVER.GENDER_RULES.get());
        List<GenderRuleConfig> result = new ArrayList<>();
        for (String rule : rules) {
            String[] parts = rule.split(";");
            if (parts.length == 3) {
                result.add(new GenderRuleConfig(parts[0], parts[1], parts[2]));
            }
        }
        return result;
    }

    public static int getDisplayRadius() {
        return SERVER.DISPLAY_RADIUS.get();
    }

    public static int getMaleChance() {
        return SERVER.MALE_CHANCE.get();
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SERVER_SPEC) {
            GenderGameplayEvents.setConfiguration(getEnabledMobs(), getGenderRules());
        }
    }

    public static class GenderRuleConfig {
        private final String mobId;
        private final String gender;
        private final String itemId;
        
        public GenderRuleConfig(String mobId, String gender, String itemId) {
            this.mobId = mobId;
            this.gender = gender;
            this.itemId = itemId;
        }
        
        public String getMobId() { return mobId; }
        public String getGender() { return gender; }
        public String getItemId() { return itemId; }
    }
}