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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@EventBusSubscriber(modid = GenderMod.MOD_ID)
public class ServerCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bub")
            .then(Commands.literal("server")
                .then(Commands.literal("reload")
                    .requires(source -> source.hasPermission(2))
                    .executes(ServerCommands::reload))
                .then(Commands.literal("integration")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.literal("default")
                        .executes(ServerCommands::integrationDefault))
                    .then(Commands.literal("reload")
                        .executes(ServerCommands::integrationReload)))
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

    private static void cleanOldBackups(Path backupDir, int maxCount) {
        try {
            if (!Files.exists(backupDir)) return;
            List<Path> files = Files.list(backupDir)
                .filter(p -> !Files.isDirectory(p))
                .sorted((a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified()))
                .collect(Collectors.toList());
            
            for (int i = maxCount; i < files.size(); i++) {
                Files.delete(files.get(i));
            }
        } catch (Exception ignored) {}
    }

    private static Path createConfigBackup(Path configPath, Path backupDir) throws Exception {
        Files.createDirectories(backupDir);
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String backupName = "genderbub_backup_" + timestamp + ".json";
        Path backupPath = backupDir.resolve(backupName);
        Files.copy(configPath, backupPath);
        cleanOldBackups(backupDir, 10);
        return backupPath;
    }

    private static Path createIntegrationBackup(Path integrationDir, Path backupDir) throws Exception {
        Files.createDirectories(backupDir);
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String backupName = "integration_backup_" + timestamp + ".zip";
        Path backupPath = backupDir.resolve(backupName);
        
        try (FileOutputStream fos = new FileOutputStream(backupPath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            Files.walk(integrationDir)
                .filter(p -> !Files.isDirectory(p))
                .forEach(p -> {
                    try {
                        String relativePath = integrationDir.relativize(p).toString().replace("\\", "/");
                        ZipEntry entry = new ZipEntry(relativePath);
                        zos.putNextEntry(entry);
                        Files.copy(p, zos);
                        zos.closeEntry();
                    } catch (Exception ignored) {}
                });
        }
        
        cleanOldBackups(backupDir, 10);
        return backupPath;
    }

    private static int integrationDefault(CommandContext<CommandSourceStack> ctx) {
        try {
            Path configPath = GenderConfig.getConfigPath();
            Path integrationDir = configPath.getParent().resolve("integration");
            Path backupDir = configPath.getParent().resolve("backups").resolve("integration");
            
            if (Files.exists(integrationDir)) {
                createIntegrationBackup(integrationDir, backupDir);
                
                Files.walk(integrationDir)
                    .filter(p -> !Files.isDirectory(p))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception ignored) {}
                    });
            }
            
            GenderLoader.mergeDefaultFiles();
            GenderLoader.loadCompatRules();
            GenderLoader.loadRules();
            GenderCache.loadFromData(GenderLoader.getData());
            
            ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("genderbub.command.reload.failed", e.getMessage()));
            return 0;
        }
    }

    private static int integrationReload(CommandContext<CommandSourceStack> ctx) {
        try {
            GenderLoader.mergeDefaultFiles();
            GenderLoader.loadCompatRules();
            GenderLoader.loadRules();
            GenderCache.loadFromData(GenderLoader.getData());
            ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("genderbub.command.reload.failed", e.getMessage()));
            return 0;
        }
    }

    private static int setMaleChance(CommandContext<CommandSourceStack> ctx) {
        int value = IntegerArgumentType.getInteger(ctx, "value");
        GenderLoader.getData().settings.maleChance = value;
        GenderLoader.save();
        GenderCache.setMaleChance(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setMaleChanceDefault(CommandContext<CommandSourceStack> ctx) {
        int value = 45;
        GenderLoader.getData().settings.maleChance = value;
        GenderLoader.save();
        GenderCache.setMaleChance(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setFemaleChance(CommandContext<CommandSourceStack> ctx) {
        int value = IntegerArgumentType.getInteger(ctx, "value");
        GenderLoader.getData().settings.femaleChance = value;
        GenderLoader.save();
        GenderCache.setFemaleChance(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setFemaleChanceDefault(CommandContext<CommandSourceStack> ctx) {
        int value = 45;
        GenderLoader.getData().settings.femaleChance = value;
        GenderLoader.save();
        GenderCache.setFemaleChance(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowMaleMaleBreed(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.allowMaleMaleBreed = value;
        GenderLoader.save();
        GenderCache.setAllowMaleMaleBreed(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowMaleMaleBreedDefault(CommandContext<CommandSourceStack> ctx) {
        boolean value = false;
        GenderLoader.getData().settings.allowMaleMaleBreed = value;
        GenderLoader.save();
        GenderCache.setAllowMaleMaleBreed(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowFemaleFemaleBreed(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.allowFemaleFemaleBreed = value;
        GenderLoader.save();
        GenderCache.setAllowFemaleFemaleBreed(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowFemaleFemaleBreedDefault(CommandContext<CommandSourceStack> ctx) {
        boolean value = false;
        GenderLoader.getData().settings.allowFemaleFemaleBreed = value;
        GenderLoader.save();
        GenderCache.setAllowFemaleFemaleBreed(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowSterileBreed(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.allowSterileBreed = value;
        GenderLoader.save();
        GenderCache.setAllowSterileBreed(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setAllowSterileBreedDefault(CommandContext<CommandSourceStack> ctx) {
        boolean value = false;
        GenderLoader.getData().settings.allowSterileBreed = value;
        GenderLoader.save();
        GenderCache.setAllowSterileBreed(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setEnableVillagers(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.enableVillagers = value;
        GenderLoader.save();
        GenderCache.setEnableVillagers(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setEnableVillagersDefault(CommandContext<CommandSourceStack> ctx) {
        boolean value = true;
        GenderLoader.getData().settings.enableVillagers = value;
        GenderLoader.save();
        GenderCache.setEnableVillagers(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setKeepVillagerGender(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        GenderLoader.getData().settings.keepVillagerGender = value;
        GenderLoader.save();
        GenderCache.setKeepVillagerGender(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int setKeepVillagerGenderDefault(CommandContext<CommandSourceStack> ctx) {
        boolean value = true;
        GenderLoader.getData().settings.keepVillagerGender = value;
        GenderLoader.save();
        GenderCache.setKeepVillagerGender(value);
        ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.config.changed"), true);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        try {
            Path configPath = GenderConfig.getConfigPath();
            if (configPath == null || !Files.exists(configPath) || Files.size(configPath) == 0) {
                ctx.getSource().sendFailure(Component.translatable("genderbub.command.reload.failed", "Config missing"));
                GenderConfig.resetToDefault();
                ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.reload.success", GenderConfig.getEnabledMobs().size()), true);
                return 1;
            }

            GenderLoader.load();
            GenderCache.loadFromData(GenderLoader.getData());

            int mobsCount = GenderConfig.getEnabledMobs().size();
            int interactionsCount = GenderConfig.getInteractionsCount();
            int eggRulesCount = GenderConfig.getEggRulesCount();
            ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.reload.synced", mobsCount, interactionsCount, eggRulesCount), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("genderbub.command.reload.failed", e.getMessage()));
            return 0;
        }
    }

    private static int scan(CommandContext<CommandSourceStack> ctx) {
        try {
            Set<String> before = GenderConfig.getEnabledMobs();
            ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.scan.start", before.size()), false);

            List<String> newAnimals = GenderConfig.scanAndGetNewAnimals();

            Set<String> after = GenderConfig.getEnabledMobs();

            if (newAnimals.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.scan.no_new", after.size()), true);
            } else {
                ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.scan.found", newAnimals.size()), true);
                ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.scan.total", after.size()), false);
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("genderbub.command.scan.failed", e.getMessage()));
            return 0;
        }
    }

    private static int resetToDefault(CommandContext<CommandSourceStack> ctx) {
        try {
            Path configPath = GenderConfig.getConfigPath();
            Path integrationDir = configPath.getParent().resolve("integration");
            Path backupDir = configPath.getParent().resolve("backups");
            
            if (Files.exists(configPath)) {
                createConfigBackup(configPath, backupDir.resolve("config"));
            }
            
            if (Files.exists(integrationDir)) {
                createIntegrationBackup(integrationDir, backupDir.resolve("integration"));
                
                Files.walk(integrationDir)
                    .filter(p -> !Files.isDirectory(p))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception ignored) {}
                    });
            }
            
            GenderLoader.reset();
            GenderLoader.mergeDefaultFiles();
            GenderLoader.loadCompatRules();
            GenderLoader.loadRules();
            GenderCache.loadFromData(GenderLoader.getData());
            
            ctx.getSource().sendSuccess(() -> Component.translatable("genderbub.command.default.success"), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("genderbub.command.default.failed", e.getMessage()));
            return 0;
        }
    }
}