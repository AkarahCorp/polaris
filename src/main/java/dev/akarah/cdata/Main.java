package dev.akarah.cdata;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.script.env.RuntimeContext;
import dev.akarah.cdata.script.exception.SpannedException;
import dev.akarah.cdata.script.jvm.CodegenContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.impl.event.lifecycle.LifecycleEventsImpl;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class Main implements ModInitializer {
    public static MinecraftServer SERVER;
    public static MinecraftServerAudiences AUDIENCES;
    public static ReloadableResourceManager CURRENT_RESOURCE_MANAGER;
    public static Logger LOGGER = LoggerFactory.getLogger("akarahnet-engine");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Main.SERVER = server;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            var root = Commands.literal("engine");

            root.then(Commands.literal("give"));

            ExtReloadableResources.customItem().registry().listElements().forEach(element -> {
                root.then(Commands.literal("give").then(Commands.literal(element.key().location().toString()).executes(ctx -> {
                    if(ctx.getSource().getEntity() instanceof Player p) {
                        p.addItem(element.value().toItemStack());
                    }
                    return 0;
                })));
            });

            ExtReloadableResources.customEntity().registry().listElements().forEach(element -> {
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

            var elements = ExtReloadableResources.actionManager().expressions()
                    .entrySet()
                    .stream()
                    .map(x -> Pair.of(x.getKey(), x.getValue()))
                    .toList();
            try {
                var codeClazz = CodegenContext.initializeCompilation(elements);

                try {
                    var method = codeClazz.getDeclaredMethod("$static_init");
                    method.invoke(null);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                elements.forEach(element -> {
                    try {
                        var resourceName = ExtReloadableResources.actionManager().resourceNames().get(element.getFirst());
                        var method = codeClazz.getDeclaredMethod(
                                element.getFirst(),
                                RuntimeContext.class
                        );
                        root.then(Commands.literal("run").then(
                                Commands.literal(resourceName.toString()).executes(ctx -> {
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
                        // if we got here, the method has parameters.
                        // just don't make it runnable in commands
                    }
                });
            } catch (SpannedException e) {
                handleError(e);
            }



            root.then(Commands.literal("my_stats").executes(ctx -> {
                if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                    var stats = ExtReloadableResources.statManager().lookup(serverPlayer);
                    ctx.getSource().sendSuccess(() -> Component.literal(stats.toString()), false);
                }
                return 0;
            }));

            dispatcher.register(root);
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
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

        if(SERVER == null) {
            System.exit(1);
        }
    }
}
