package dev.akarah.cdata;

import java.io.IOException;
import java.lang.invoke.WrongMethodTypeException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import dev.akarah.cdata.script.value.RNullable;
import dev.akarah.cdata.script.value.RuntimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.datafixers.util.Pair;

import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.exception.SpannedException;
import dev.akarah.cdata.script.expr.docs.DocBuilder;
import dev.akarah.cdata.script.value.mc.REntity;
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
    public static Logger LOGGER = LoggerFactory.getLogger("akarahnet-engine");

    @Override
    public void onInitialize() {
        RuntimeValue.dict();

        var docs = DocBuilder.builder().addAllTypes().build();

        var path = Paths.get("./DOCS.md");

        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Files.writeString(path, docs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Main.SERVER = server;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
            Resources.command().registry().listElements().forEach(element -> {
                var baseId = element.key().location().toString();

                var root = Commands.literal(baseId);
                element.value().dispatch(root);

                System.out.println(root.build());
                dispatcher.register(root);
            });


            var root = Commands.literal("engine").requires(x -> x.hasPermission(4));

            root.then(Commands.literal("give"));

            Resources.customItem().registry().listElements().forEach(element -> {
                root.then(Commands.literal("give").then(Commands.literal(element.key().location().toString()).executes(ctx -> {
                    try {
                        if(ctx.getSource().getEntity() instanceof Player p) {
                            p.addItem(element.value().toItemStack(RNullable.of(REntity.of(p))));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return 0;
                })));
            });

            Resources.customEntity().registry().listElements().forEach(element -> {
                root.then(Commands.literal("summon").then(Commands.literal(element.key().location().toString())
                        .executes(ctx -> {
                            try {
                                if(ctx.getSource().getEntity() instanceof Player p) {
                                    element.value().spawn(p.level(), p.getPosition(0.0f));
                                }
                            } catch (RuntimeException exception) {
                                exception.printStackTrace();
                            }
                            return 0;
                        })));
            });

            var elements = Resources.actionManager().expressions()
                    .entrySet()
                    .stream()
                    .map(x -> Pair.of(x.getKey(), x.getValue()))
                    .toList();
            try {
                try {
                    var methodHandle = Resources.actionManager().methodHandleByRawName("$static_init");
                    methodHandle.invoke();
                    elements.forEach(element -> {
                        try {
                            var resourceName = Resources.actionManager().resourceNames().get(element.getFirst());
                            var method = Resources.actionManager().methodHandleByLocation(resourceName);
                            if(method.type().parameterCount() != 1 && method.type().parameterType(0).equals(REntity.class)) {
                                return;
                            }
                            root.then(Commands.literal("run").then(
                                    Commands.literal(resourceName.toString()).executes(ctx -> {
                                        if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                            try {
                                                var start = System.nanoTime()/1000000.0;
                                                method.invoke(REntity.of(serverPlayer));
                                                var end = System.nanoTime()/1000000.0;
                                                ctx.getSource().sendSuccess(() -> Component.literal("Script execution took " + (end - start) + "ms"), true);
                                            } catch (Throwable e) {
                                                if(e instanceof WrongMethodTypeException) {
                                                    ctx.getSource().sendFailure(Component.literal("Method " + resourceName + " must take 1 parameter of type `entity`!"));
                                                } else {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        return 0;
                                    })
                            ));
                        } catch (Exception e) {
                            // if we got here, the method has parameters.
                            // just don't make it runnable in commands
                        }
                    });
                } catch (Throwable _) {
                    // ignore it here, since invoking $static_init failed :(
                    // that means we just say no actions exist and move on with our lives
                }
            } catch (SpannedException e) {
                handleError(e);
            }



            root.then(Commands.literal("my_stats").executes(ctx -> {
                if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                    var stats = Resources.statManager().lookup(serverPlayer);
                    ctx.getSource().sendSuccess(() -> Component.literal(stats.toString()), false);
                }
                return 0;
            }));

            dispatcher.register(root);
        });

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
        if(Objects.equals(System.getenv("POLARIS_PRINT_STACKTRACE_ON_COMPILE_FAIL"), "1")) {
            e.printStackTrace();
        }

        if(SERVER == null) {
            System.exit(1);
        }
    }
}
