package dev.akarah.polaris;

import dev.akarah.polaris.commands.CommandEventHandler;
import dev.akarah.polaris.devext.DevExtensionServer;
import dev.akarah.polaris.io.ExceptionPrinter;
import dev.akarah.polaris.script.value.RuntimeValue;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.net.InetSocketAddress;

public class Main implements ModInitializer {
    public static MinecraftServer SERVER;
    public static Logger LOGGER = LoggerFactory.getLogger("polaris");

    @Override
    public void onInitialize() {
        Thread.ofVirtual().start(() -> {
            var socket = new DevExtensionServer(new InetSocketAddress("localhost", 47474));
            socket.start();

            ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
                try {
                    socket.stop();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });

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
                            PermissionSet.ALL_PERMISSIONS,
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
