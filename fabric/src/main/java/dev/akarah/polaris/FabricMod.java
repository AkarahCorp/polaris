package dev.akarah.polaris;

import dev.akarah.polaris.building.PSCommands;
import dev.akarah.polaris.commands.CommandEventHandler;
import dev.akarah.polaris.devext.DevExtensionServer;
import dev.akarah.polaris.script.value.RuntimeValue;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class FabricMod implements ModInitializer {
    public static MinecraftServer SERVER;
    public static Logger LOGGER = LoggerFactory.getLogger("polaris");

    @Override
    public void onInitialize() {
        Thread.ofVirtual().start(() -> {
            var socket = new DevExtensionServer(new InetSocketAddress("localhost", 47474));
            socket.begin();

            ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
                socket.end();
            });
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, _) -> {
            var root = Commands.literal("engine").requires(x -> x.permissions().hasPermission(Permissions.COMMANDS_ADMIN));

            for(var source : CommandEventHandler.commandSources()) {
                source.accept(dispatcher, root);
            }

            dispatcher.register(root);

            PSCommands.register(dispatcher, context);
        });

        RuntimeValue.dict();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ((RangedAttribute) Attributes.MAX_HEALTH.value()).maxValue = Double.MAX_VALUE;
            FabricMod.SERVER = server;
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

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, _, success) -> {
            if(success) {
                for(var player : server.getPlayerList().getPlayers()) {
                    server.getCommands().sendCommands(player);
                }
            }
        });
    }
}
