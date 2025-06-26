package dev.akarah.cdata;

import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.registry.stat.StatManager;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.env.Selection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.time.Duration;
import java.time.Instant;

public class Main implements ModInitializer {
    public static MinecraftServer SERVER;
    public static MinecraftServerAudiences AUDIENCES;
    static StatManager STAT_MANAGER;

    @Override
    public void onInitialize() {
        Main.STAT_MANAGER = new StatManager();
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

            context.lookupOrThrow(ExtRegistries.META_CODEC).listElements().forEach(element -> {
                root.then(Commands.literal("checkwithcodec").then(
                        Commands.literal(element.key().location().toString()).then(
                                Commands.argument("value", NbtTagArgument.nbtTag()).executes(ctx -> {
                                    try {
                                        var value = NbtTagArgument.getNbtTag(ctx, "value");
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

            context.lookupOrThrow(ExtRegistries.SCRIPT).listElements().forEach(element -> {
                root.then(Commands.literal("script").then(
                        Commands.literal(element.key().location().toString()).executes(ctx -> {
                            ctx.getSource().sendSystemMessage(Component.literal("Starting!"));
                            var scriptSelection = Selection.of(ctx.getSource().getEntity());
                            var scriptContext = ScriptContext.of(scriptSelection);

                            var start = Instant.now();
                            element.value().execute(scriptContext);
                            var end = Instant.now();

                            var nanos = Duration.between(start, end).toNanos();
                            var milliseconds = ((double) nanos) / 1000000.0;
                            ctx.getSource().sendSystemMessage(Component.literal("Took " + milliseconds + "ms"));

                            return 0;
                        })
                ));
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
}
