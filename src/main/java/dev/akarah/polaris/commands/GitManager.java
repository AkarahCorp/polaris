package dev.akarah.polaris.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.akarah.polaris.Main;
import dev.akarah.polaris.io.ExceptionPrinter;
import dev.akarah.polaris.io.PlayerOutputStream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class GitManager {
    public static String GITHUB_TOKEN;
    public static String GITHUB_USER;
    public static File MAIN_DATAPACK_DIR;

    public static List<BiConsumer<CommandDispatcher<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>>> gitCommands() {
        return List.of(
                GitManager::pullBranch
        );
    }
    static {
        try {
            GITHUB_TOKEN = Files.readString(Paths.get("./.polaris/github.token"));
        } catch (IOException exception) {
            ExceptionPrinter.writeExceptionToOps(exception);
            Main.LOGGER.warn("No github token found, git commands will not work.");
        }
        try {
            GITHUB_USER = Files.readString(Paths.get("./.polaris/github.user"));
        } catch (IOException exception) {
            ExceptionPrinter.writeExceptionToOps(exception);
            Main.LOGGER.warn("No github username found, git commands will not work.");
        }
        try {
            MAIN_DATAPACK_DIR = new File("world/datapacks/" + Files.readString(Paths.get("./.polaris/datapack.token")) + "/");
        } catch (IOException exception) {
            ExceptionPrinter.writeExceptionToOps(exception);
            Main.LOGGER.warn("No datapack token found, or it is invalid, git commands will not work.");
        }
    }

    public static Process executeCommand(ServerPlayer executor, String... command) throws IOException {
        executor.sendSystemMessage(Component.literal("Executing `" + Arrays.toString(command) + "`...").withColor(ARGB.color(200, 200, 200)));
        var proc = new ProcessBuilder().command(command).directory(MAIN_DATAPACK_DIR).start();
        proc.getInputStream().transferTo(new PlayerOutputStream(executor));
        proc.getErrorStream().transferTo(new PlayerOutputStream(executor));
        return proc;
    }

    public static void pullBranch(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(
                Commands.literal("gh").then(
                        Commands.literal("checkout-pull-request").then(
                                Commands.argument("branch", IntegerArgumentType.integer(0, Integer.MAX_VALUE)).executes(ctx -> {
                                    if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                        if(!MAIN_DATAPACK_DIR.exists()) {
                                            serverPlayer.sendSystemMessage(Component.literal("Datapack directory does not exist!").withColor(ARGB.color(255, 0, 0)));
                                            serverPlayer.sendSystemMessage(Component.literal("The full datapack directory is `" + MAIN_DATAPACK_DIR.getAbsolutePath() + "`.").withColor(ARGB.color(255, 0, 0)));
                                            return 1;
                                        }
                                        try {
                                            var branch = IntegerArgumentType.getInteger(ctx, "branch");

                                            executeCommand(serverPlayer, "git", "fetch");
                                            executeCommand(serverPlayer, "git", "checkout", "main");
                                            executeCommand(serverPlayer, "git", "reset", "--hard", "origin");
                                            executeCommand(serverPlayer, "git", "branch", "-D", "polaris-demo");
                                            executeCommand(serverPlayer, "git", "branch", "polaris-demo");
                                            executeCommand(serverPlayer, "git", "checkout", "polaris-demo");

                                            executeCommand(serverPlayer, "git", "pull", "origin", "refs/pull/" + branch + "/head");
                                        } catch (IOException e) {
                                            ExceptionPrinter.writeExceptionToOps(e);
                                        }
                                    }
                                    return 0;
                                })
                        )
                ).then(
                        Commands.literal("main").executes(ctx -> {
                            if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                if(!MAIN_DATAPACK_DIR.exists()) {
                                    serverPlayer.sendSystemMessage(Component.literal("Datapack directory does not exist!").withColor(ARGB.color(255, 0, 0)));
                                    serverPlayer.sendSystemMessage(Component.literal("The full datapack directory is `" + MAIN_DATAPACK_DIR.getAbsolutePath() + "`.").withColor(ARGB.color(255, 0, 0)));
                                    return 1;
                                }
                                try {
                                    executeCommand(serverPlayer, "git", "checkout", "main");
                                    executeCommand(serverPlayer, "git", "reset", "--hard", "origin");
                                    executeCommand(serverPlayer, "git", "branch", "-D", "polaris-demo");
                                } catch (IOException e) {
                                    ExceptionPrinter.writeExceptionToOps(e);
                                }
                            }
                            return 0;
                        })
                ).then(
                        Commands.literal("status").executes(ctx -> {
                            if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                if(!MAIN_DATAPACK_DIR.exists()) {
                                    serverPlayer.sendSystemMessage(Component.literal("Datapack directory does not exist!").withColor(ARGB.color(255, 0, 0)));
                                    serverPlayer.sendSystemMessage(Component.literal("The full datapack directory is `" + MAIN_DATAPACK_DIR.getAbsolutePath() + "`.").withColor(ARGB.color(255, 0, 0)));
                                    return 1;
                                }
                                try {
                                    executeCommand(serverPlayer, "git", "status");
                                } catch (IOException e) {
                                    ExceptionPrinter.writeExceptionToOps(e);
                                }
                            }
                            return 0;
                        })
                ).then(
                        Commands.literal("branches").executes(ctx -> {
                            if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                if(!MAIN_DATAPACK_DIR.exists()) {
                                    serverPlayer.sendSystemMessage(Component.literal("Datapack directory does not exist!").withColor(ARGB.color(255, 0, 0)));
                                    serverPlayer.sendSystemMessage(Component.literal("The full datapack directory is `" + MAIN_DATAPACK_DIR.getAbsolutePath() + "`.").withColor(ARGB.color(255, 0, 0)));
                                    return 1;
                                }
                                try {
                                    executeCommand(serverPlayer, "git", "branch");
                                } catch (IOException e) {
                                    ExceptionPrinter.writeExceptionToOps(e);
                                }
                            }
                            return 0;
                        })
                ).then(
                        Commands.literal("log").executes(ctx -> {
                            if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                if(!MAIN_DATAPACK_DIR.exists()) {
                                    serverPlayer.sendSystemMessage(Component.literal("Datapack directory does not exist!").withColor(ARGB.color(255, 0, 0)));
                                    serverPlayer.sendSystemMessage(Component.literal("The full datapack directory is `" + MAIN_DATAPACK_DIR.getAbsolutePath() + "`.").withColor(ARGB.color(255, 0, 0)));
                                    return 1;
                                }
                                try {
                                    executeCommand(serverPlayer, "git", "log");
                                } catch (IOException e) {
                                    ExceptionPrinter.writeExceptionToOps(e);
                                }
                            }
                            return 0;
                        })
                )
        );
    }
}
