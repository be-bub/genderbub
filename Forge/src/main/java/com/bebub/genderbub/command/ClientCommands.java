package com.bebub.genderbub.command;

import com.bebub.genderbub.GenderMod;
import com.bebub.genderbub.config.GenderLoader;
import com.bebub.genderbub.config.GenderCache;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GenderMod.MOD_ID, value = Dist.CLIENT)
public class ClientCommands {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(Commands.literal("bub")
            .then(Commands.literal("client")
                .then(Commands.literal("reload")
                    .executes(ClientCommands::clientReload))
                .then(Commands.literal("default")
                    .executes(ClientCommands::clientResetToDefault))
                .then(Commands.literal("displayRadius")
                    .then(Commands.argument("value", IntegerArgumentType.integer(0, 128))
                        .executes(ClientCommands::setDisplayRadius))
                    .then(Commands.literal("default")
                        .executes(ClientCommands::setDisplayRadiusDefault)))
                .then(Commands.literal("hideWithJade")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ClientCommands::setHideWithJade))
                    .then(Commands.literal("default")
                        .executes(ClientCommands::setHideWithJadeDefault)))
                .then(Commands.literal("requireScanner")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ClientCommands::setRequireScanner))
                    .then(Commands.literal("default")
                        .executes(ClientCommands::setRequireScannerDefault)))
                .then(Commands.literal("syncConfigRules")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ClientCommands::setSyncConfigRules))
                    .then(Commands.literal("default")
                        .executes(ClientCommands::setSyncConfigRulesDefault))))
        );
    }

    private static int clientReload(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.load();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), false);
        return 1;
    }

    private static int clientResetToDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.displayRadius = 24;
        GenderLoader.getData().settings.hideWithJade = true;
        GenderLoader.getData().settings.requireScanner = true;
        GenderLoader.getData().settings.syncConfigRules = false;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), false);
        return 1;
    }

    private static int setDisplayRadius(CommandContext<CommandSourceStack> ctx) {
        int value = IntegerArgumentType.getInteger(ctx, "value");
        GenderLoader.getData().settings.displayRadius = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), false);
        return 1;
    }

    private static int setDisplayRadiusDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.displayRadius = 24;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), false);
        return 1;
    }

    private static int setHideWithJade(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.hideWithJade = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), false);
        return 1;
    }

    private static int setHideWithJadeDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.hideWithJade = true;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), false);
        return 1;
    }

    private static int setRequireScanner(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.requireScanner = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), false);
        return 1;
    }

    private static int setRequireScannerDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.requireScanner = true;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), false);
        return 1;
    }

    private static int setSyncConfigRules(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.syncConfigRules = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), false);
        return 1;
    }

    private static int setSyncConfigRulesDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.syncConfigRules = false;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), false);
        return 1;
    }
}