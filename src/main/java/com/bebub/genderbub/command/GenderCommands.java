package com.bebub.genderbub.command;

import com.bebub.genderbub.GenderMod;
import com.bebub.genderbub.GenderGameplayEvents;
import com.bebub.genderbub.client.ClientGenderCache;
import com.bebub.genderbub.config.GenderConfig;
import com.bebub.genderbub.network.EnabledMobsSyncPacket;
import com.bebub.genderbub.network.NetworkHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = GenderMod.MOD_ID)
public class GenderCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(Commands.literal("bub")
            .then(Commands.literal("reload")
                .requires(source -> source.hasPermission(2))
                .executes(GenderCommands::reload))
            .then(Commands.literal("reset")
                .requires(source -> source.hasPermission(2))
                .executes(GenderCommands::reset))
            .then(Commands.literal("scan")
                .requires(source -> source.hasPermission(2))
                .executes(GenderCommands::scan))
            .then(Commands.literal("clear")
                .requires(source -> source.hasPermission(0))
                .executes(GenderCommands::clearCache))
        );
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        try {
            Path configPath = GenderConfig.getConfigPath();
            if (configPath == null || !Files.exists(configPath) || Files.size(configPath) == 0) {
                ctx.getSource().sendFailure(Component.literal("Config missing, creating new one..."));
                GenderConfig.resetToDefault();
                ctx.getSource().sendSuccess(() -> Component.literal("New config created!"), true);
                return 1;
            }
            
            GenderConfig.reload();
            GenderGameplayEvents.reloadConfig();
            
            Set<String> enabledMobs = new HashSet<>(GenderConfig.getEnabledMobs());
            
            for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EnabledMobsSyncPacket(enabledMobs)
                );
            }
            
            ctx.getSource().sendSuccess(() -> Component.literal("Config reloaded! " + enabledMobs.size() + " mobs enabled"), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed to reload config: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int reset(CommandContext<CommandSourceStack> ctx) {
        try {
            GenderConfig.resetToDefault();
            GenderGameplayEvents.reloadConfig();
            
            Set<String> emptyMobList = new HashSet<>();
            
            for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EnabledMobsSyncPacket(emptyMobList)
                );
            }
            
            ctx.getSource().sendSuccess(() -> Component.literal("Config reset to default!"), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed to reset config: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int scan(CommandContext<CommandSourceStack> ctx) {
        try {
            List<String> before = GenderConfig.getEnabledMobs();
            ctx.getSource().sendSuccess(() -> Component.literal("Current enabled animals: " + before.size()), false);
            
            List<String> newAnimals = GenderConfig.scanAndGetNewAnimals();
            GenderGameplayEvents.reloadConfig();
            
            List<String> after = GenderConfig.getEnabledMobs();
            
            if (!newAnimals.isEmpty()) {
                Set<String> enabledMobs = new HashSet<>(GenderConfig.getEnabledMobs());
                for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                    NetworkHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new EnabledMobsSyncPacket(enabledMobs)
                    );
                }
            }
            
            if (newAnimals.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.literal("No new animals found! Total enabled: " + after.size()), true);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("Found and added " + newAnimals.size() + " new animals!"), true);
                ctx.getSource().sendSuccess(() -> Component.literal("Total enabled animals: " + after.size()), false);
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed to scan: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int clearCache(CommandContext<CommandSourceStack> ctx) {
        try {
            ClientGenderCache.clear();
            ctx.getSource().sendSuccess(() -> Component.literal("Gender cache cleared!"), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Failed to clear cache: " + e.getMessage()));
            return 0;
        }
    }
}