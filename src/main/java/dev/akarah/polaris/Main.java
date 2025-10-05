package dev.akarah.polaris;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.WrongMethodTypeException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.akarah.polaris.commands.CommandEventHandler;
import dev.akarah.polaris.io.ExceptionPrinter;
import dev.akarah.polaris.io.PlayerWriter;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.value.RNullable;
import dev.akarah.polaris.script.value.RNumber;
import dev.akarah.polaris.script.value.RString;
import dev.akarah.polaris.script.value.RuntimeValue;
import dev.akarah.polaris.script.value.mc.RItem;
import dev.akarah.polaris.script.value.mc.rt.DslProfiler;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.datafixers.util.Pair;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.exception.SpannedException;
import dev.akarah.polaris.script.expr.docs.DocBuilder;
import dev.akarah.polaris.script.value.mc.REntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class Main implements ModInitializer {
    public static MinecraftServer SERVER;
    public static Logger LOGGER = LoggerFactory.getLogger("polaris");

    @Override
    public void onInitialize() {
        RuntimeValue.dict();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ((RangedAttribute) Attributes.MAX_HEALTH.value()).maxValue = Double.MAX_VALUE;
            Main.SERVER = server;
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            server.getCommands().performCommand(
                    server.getCommands().getDispatcher().parse("reload", new CommandSourceStack(
                            CommandSource.NULL,
                            Vec3.ZERO,
                            Vec2.ZERO,
                            server.overworld(),
                            4,
                            "console",
                            Component.literal("console"),
                            server,
                            null
                    )),
                    "reload"
            );
        });

        CommandEventHandler.register();

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, _, success) -> {
            if(success) {
                for(var player : server.getPlayerList().getPlayers()) {
                    server.getCommands().sendCommands(player);
                }
            }
        });
    }

    public static MinecraftServer server() {
        return SERVER;
    }

    public static void handleError(Exception e) {
        String sb = "\n"
                + "A fatal error occurred while generating code! Server may fail to start."
                + "\n" + e.getMessage()
                + "\n";

        LOGGER.error(sb);

        try {
            ExceptionPrinter.writeExceptionToOps(e);
        } catch (Exception ignored) {

        }

        if(SERVER == null) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
