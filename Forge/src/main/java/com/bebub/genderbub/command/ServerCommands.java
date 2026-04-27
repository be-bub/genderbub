package com.bebub.genderbub.command;

import com.bebub.genderbub.GenderMod;
import com.bebub.genderbub.config.GenderConfig;
import com.bebub.genderbub.config.GenderLoader;
import com.bebub.genderbub.config.GenderCache;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = GenderMod.MOD_ID)
public class ServerCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bub")
            .then(Commands.literal("server")
                .then(Commands.literal("reload")
                    .requires(source -> source.hasPermission(2))
                    .executes(ServerCommands::reload))
                .then(Commands.literal("scan")
                    .requires(source -> source.hasPermission(2))
                    .executes(ServerCommands::scan))
                .then(Commands.literal("default")
                    .requires(source -> source.hasPermission(2))
                    .executes(ServerCommands::resetToDefault))
                .then(Commands.literal("maleChance")
                    .then(Commands.argument("value", IntegerArgumentType.integer(0, 50))
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setMaleChance))
                    .then(Commands.literal("default")
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setMaleChanceDefault)))
                .then(Commands.literal("femaleChance")
                    .then(Commands.argument("value", IntegerArgumentType.integer(0, 50))
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setFemaleChance))
                    .then(Commands.literal("default")
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setFemaleChanceDefault)))
                .then(Commands.literal("allowMaleMaleBreed")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setAllowMaleMaleBreed))
                    .then(Commands.literal("default")
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setAllowMaleMaleBreedDefault)))
                .then(Commands.literal("allowFemaleFemaleBreed")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setAllowFemaleFemaleBreed))
                    .then(Commands.literal("default")
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setAllowFemaleFemaleBreedDefault)))
                .then(Commands.literal("allowSterileBreed")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setAllowSterileBreed))
                    .then(Commands.literal("default")
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setAllowSterileBreedDefault)))
                .then(Commands.literal("enableVillagers")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setEnableVillagers))
                    .then(Commands.literal("default")
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setEnableVillagersDefault)))
                .then(Commands.literal("keepVillagerGender")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setKeepVillagerGender))
                    .then(Commands.literal("default")
                        .requires(source -> source.hasPermission(2))
                        .executes(ServerCommands::setKeepVillagerGenderDefault))))
        );
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static int setMaleChance(CommandContext<CommandSourceStack> ctx) {
        int value = IntegerArgumentType.getInteger(ctx, "value");
        GenderLoader.getData().settings.maleChance = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setMaleChanceDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.maleChance = 45;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setFemaleChance(CommandContext<CommandSourceStack> ctx) {
        int value = IntegerArgumentType.getInteger(ctx, "value");
        GenderLoader.getData().settings.femaleChance = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setFemaleChanceDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.femaleChance = 45;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowMaleMaleBreed(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.allowMaleMaleBreed = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowMaleMaleBreedDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.allowMaleMaleBreed = false;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowFemaleFemaleBreed(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.allowFemaleFemaleBreed = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowFemaleFemaleBreedDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.allowFemaleFemaleBreed = false;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowSterileBreed(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.allowSterileBreed = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowSterileBreedDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.allowSterileBreed = false;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setEnableVillagers(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.enableVillagers = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setEnableVillagersDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.enableVillagers = true;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setKeepVillagerGender(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.keepVillagerGender = value;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setKeepVillagerGenderDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.getData().settings.keepVillagerGender = true;
        GenderLoader.save();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.load();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.reload.synced", 
            GenderConfig.getEnabledMobs().size(), 
            GenderConfig.getInteractionsCount(), 
            GenderConfig.getEggRulesCount()), true);
        return 1;
    }

    private static int scan(CommandContext<CommandSourceStack> ctx) {
        List<String> newAnimals = GenderConfig.scanAndGetNewAnimals();
        if (newAnimals.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.scan.no_new", GenderConfig.getEnabledMobs().size()), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.scan.found", newAnimals.size()), true);
        }
        return 1;
    }

    private static int resetToDefault(CommandContext<CommandSourceStack> ctx) {
        GenderLoader.reset();
        GenderCache.loadFromData(GenderLoader.getData());
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.default.success"), true);
        return 1;
    }
}