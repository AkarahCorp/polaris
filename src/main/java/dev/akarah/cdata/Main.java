package dev.akarah.cdata;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.registry.stat.StatManager;
import dev.akarah.cdata.script.env.RuntimeContext;
import dev.akarah.cdata.script.jvm.CodegenContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;

public class Main implements ModInitializer {
    public static MinecraftServer SERVER;
    public static MinecraftServerAudiences AUDIENCES;
    static StatManager STAT_MANAGER;
    static EngineConfig CONFIG;

    @Override
    public void onInitialize() {
        var engineConfigPath = Paths.get("./engine.json");
        if(Files.exists(engineConfigPath)) {
            var json = JsonParser.parseString(Util.sneakyThrows(() -> Files.readString(engineConfigPath)));
            CONFIG = EngineConfig.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
        }

        Main.STAT_MANAGER = new StatManager();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Main.SERVER = server;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {

            dispatcher.register(Commands.literal("test").executes(ctx -> {
                ctx.getSource().sendSystemMessage(Component.literal(
                        Main.SERVER.registryAccess()
                                .lookupOrThrow(ExtRegistries.CUSTOM_ITEM)
                                .entrySet()
                                .toString()
                ));
                return 0;
            }));
            var root = Commands.literal("engine");

            context.lookupOrThrow(ExtRegistries.CUSTOM_ITEM).listElements().forEach(element -> {
                root.then(Commands.literal("give").then(Commands.literal(element.key().location().toString()).executes(ctx -> {
                    try {
                        if(ctx.getSource().getEntity() instanceof Player p) {
                            p.addItem(element.value().toItemStack());
                        }
                    } catch (RuntimeException exception) {
                        exception.printStackTrace();
                    }
                    return 0;
                })));
            });

            context.lookupOrThrow(ExtRegistries.CUSTOM_ENTITY).listElements().forEach(element -> {
                root.then(Commands.literal("summon").then(Commands.literal(element.key().location().toString()).executes(ctx -> {
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

            context.lookupOrThrow(ExtRegistries.META_CODEC).listElements().forEach(element -> {
                root.then(Commands.literal("checkwithcodec").then(
                        Commands.literal(element.key().location().toString()).then(
                                Commands.argument("item", NbtTagArgument.nbtTag()).executes(ctx -> {
                                    try {
                                        var value = NbtTagArgument.getNbtTag(ctx, "item");
                                        var result = element.value().codec().decode(NbtOps.INSTANCE, value);
                                        if(result.isError()) {
                                            ctx.getSource().sendFailure(
                                                    Component.literal("Failed: " + result.error().orElseThrow().message())
                                            );
                                            return 1;
                                        } else {
                                            ctx.getSource().sendSuccess(() -> Component.literal("Success!"), true);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return 0;
                                })
                        )
                ));
            });

            var elements = context.lookupOrThrow(ExtRegistries.SCRIPT).listElements().toList();
            var codeClazz = CodegenContext.initializeCompilation(elements);

            try {
                var method = codeClazz.getDeclaredMethod("$static_init");
                method.invoke(null);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            elements.forEach(element -> {
                try {
                    var method = codeClazz.getDeclaredMethod(
                            CodegenContext.resourceLocationToMethodName(
                                    element.key().location()
                            ),
                            RuntimeContext.class
                    );
                    root.then(Commands.literal("run").then(
                            Commands.literal(element.key().location().toString()).executes(ctx -> {
                                if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                    try {
                                        var start = System.nanoTime()/1000000.0;
                                        method.invoke(null, RuntimeContext.of(serverPlayer));
                                        var end = System.nanoTime()/1000000.0;
                                        ctx.getSource().sendSuccess(() -> Component.literal("Script execution took " + (end - start) + "ms"), true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                return 0;
                            })
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            root.then(Commands.literal("my_stats").executes(ctx -> {
                if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                    var stats = Main.statManager().lookup(serverPlayer);
                    ctx.getSource().sendSuccess(() -> Component.literal(stats.toString()), false);
                }
                return 0;
            }));

            dispatcher.register(root);
        });
    }

    public static MinecraftServer server() {
        return SERVER;
    }

    public static StatManager statManager() {
        return STAT_MANAGER;
    }

    public static EngineConfig config() {
        return CONFIG;
    }
}
