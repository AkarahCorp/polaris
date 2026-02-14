package dev.akarah.polaris.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import dev.akarah.polaris.Main;
import dev.akarah.polaris.building.PSCommands;
import dev.akarah.polaris.io.ExceptionPrinter;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.command.ArgumentCommandNode;
import dev.akarah.polaris.script.exception.SpannedException;
import dev.akarah.polaris.script.value.RNullable;
import dev.akarah.polaris.script.value.RNumber;
import dev.akarah.polaris.script.value.RString;
import dev.akarah.polaris.script.value.mc.REntity;
import dev.akarah.polaris.script.value.mc.RItem;
import dev.akarah.polaris.script.value.polaris.DslProfiler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.lang.invoke.WrongMethodTypeException;
import java.util.List;
import java.util.function.BiConsumer;

public class CommandEventHandler {

    public static List<BiConsumer<CommandDispatcher<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>>> commandSources() {
        var list = Lists.<BiConsumer<CommandDispatcher<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>>>newArrayList();
        list.addAll(CommandEventHandler.engineCommands());
        list.addAll(GitManager.gitCommands());
        return list;
    }

    public static List<BiConsumer<CommandDispatcher<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>>> engineCommands() {
        return List.of(
                CommandEventHandler::registerCustomCommands,
                CommandEventHandler::giveCommand,
                CommandEventHandler::setTagCommand,
                CommandEventHandler::myStatsCommand,
                CommandEventHandler::summonCommand,
                CommandEventHandler::runCommand,
                CommandEventHandler::profilerCommand
        );
    }

    public static void registerCustomCommands(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> root) {
        Resources.command().registry().listElements().forEach(element -> {
            var baseId = element.key().identifier().getPath();

            var root2 = Commands.literal(baseId);
            element.value().dispatch(root2);

            dispatcher.register(root2);
        });
    }

    public static void giveCommand(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("give"));

        Resources.customItem().registry().listElements().forEach(element -> {
            root.then(Commands.literal("give").then(Commands.literal(element.key().identifier().toString()).executes(ctx -> {
                try {
                    if(ctx.getSource().getEntity() instanceof Player p) {
                        p.addItem(element.value().toItemStack(RNullable.of(REntity.of(p))));
                    }
                } catch (Exception e) {
                    ExceptionPrinter.writeException(ctx.getSource().getPlayer(), e);
                }
                return 0;
            })));
            root.then(Commands.literal("give").then(Commands.literal(element.key().identifier().toString()).then(
                    Commands.argument("count", IntegerArgumentType.integer()).executes(ctx -> {
                        try {
                            if(ctx.getSource().getEntity() instanceof Player p) {
                                var is = element.value().toItemStack(RNullable.of(REntity.of(p)));
                                is.setCount(ctx.getArgument("count", Integer.class));
                                p.addItem(is);
                            }
                        } catch (Exception e) {
                            ExceptionPrinter.writeException(ctx.getSource().getPlayerOrException(), e);
                        }
                        return 0;
                    })
            )));
        });
    }

    public static void summonCommand(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> root) {
        Resources.customEntity().registry().listElements().forEach(element -> {
            root.then(Commands.literal("summon").then(Commands.literal(element.key().identifier().toString())
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
    }

    public static void runCommand(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> root) {
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
                        var method = Resources.actionManager().methodHandleByLocation(element.getFirst());
                        if(method.type().parameterCount() != 1 && method.type().parameterType(0).equals(REntity.class)) {
                            return;
                        }
                        root.then(Commands.literal("run").then(
                                Commands.literal(element.getFirst().toString()).executes(ctx -> {
                                    if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                        try {
                                            var start = System.nanoTime()/1000000.0;
                                            method.invoke(REntity.of(serverPlayer));
                                            var end = System.nanoTime()/1000000.0;
                                            ctx.getSource().sendSuccess(() -> Component.literal("Script execution took " + (end - start) + "ms"), true);
                                        } catch (Throwable e) {
                                            if(e instanceof WrongMethodTypeException) {
                                                ctx.getSource().sendFailure(Component.literal("Method " + element.getFirst() + " must take 1 parameter of type `entity`!"));
                                            } else {
                                                ExceptionPrinter.writeException(ctx.getSource().getPlayerOrException(), e);
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
            Main.handleError(e);
        }
    }

    public static void myStatsCommand(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("my_stats").executes(ctx -> {
            if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                var stats = Resources.statManager().lookup(serverPlayer);
                ctx.getSource().sendSuccess(() -> Component.literal(stats.toString()), false);
            }
            return 0;
        }));
    }

    public static void profilerCommand(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(
                Commands.literal("profiler").then(
                        Commands.literal("set").then(
                                Commands.argument("name_filter", StringArgumentType.string()).then(
                                        Commands.argument("duration_in_microseconds", DoubleArgumentType.doubleArg()).executes(ctx -> {
                                            if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                                var filter = new DslProfiler.DslProfileFilter(
                                                        ctx.getArgument("name_filter", String.class),
                                                        ctx.getArgument("duration_in_microseconds", Double.class)
                                                );
                                                DslProfiler.profileSourceFilters.put(serverPlayer.getUUID(), filter);
                                            }
                                            return 0;
                                        })
                                )
                        ).then(
                                Commands.literal("reset")).executes(ctx -> {
                                    if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                        DslProfiler.profileSourceFilters.remove(serverPlayer.getUUID());
                                    }
                                    return 0;
                                })
                        )
                );
    }

    public static void setTagCommand(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(
                Commands.literal("set_tag").then(
                        Commands.argument("key", StringArgumentType.string()).then(
                                Commands.argument("number", DoubleArgumentType.doubleArg()).executes(ctx -> {
                                    if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                        var itemInHand = serverPlayer.getItemBySlot(EquipmentSlot.MAINHAND);
                                        var rItem = RItem.of(itemInHand);
                                        RItem.set_tag(
                                                rItem,
                                                RString.of(ctx.getArgument("key", String.class)),
                                                RNumber.of(ctx.getArgument("number", Double.class))
                                        );
                                        RItem.update(rItem, RNullable.of(REntity.of(serverPlayer)));
                                        Resources.statManager().refreshPlayerInventories();
                                    }
                                    return 0;
                                })
                        ).then(
                                Commands.argument("string", StringArgumentType.string()).executes(ctx -> {
                                    if(ctx.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
                                        var itemInHand = serverPlayer.getItemBySlot(EquipmentSlot.MAINHAND);
                                        var rItem = RItem.of(itemInHand);
                                        RItem.set_tag(
                                                rItem,
                                                RString.of(ctx.getArgument("key", String.class)),
                                                RString.of(ctx.getArgument("string", String.class))
                                        );
                                        RItem.update(rItem, RNullable.of(REntity.of(serverPlayer)));
                                        serverPlayer.setItemSlot(EquipmentSlot.MAINHAND, rItem.javaValue());
                                        Resources.statManager().refreshPlayerInventories();
                                    }
                                    return 0;
                                })
                        )
                )
        );
    }
}
