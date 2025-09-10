package dev.akarah.polaris.io;

import dev.akarah.polaris.Main;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.jvm.CodegenContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.animal.Cod;

public class ExceptionPrinter {
    public static void writeExceptionToOps(Throwable t) {
        for(var player : Main.server().getPlayerList().getPlayers()) {
            if(player.hasPermissions(2)) {
                writeException(player, t, null);
            }
        }
    }

    public static void writeExceptionToOps(Throwable t, ResourceLocation file) {
        for(var player : Main.server().getPlayerList().getPlayers()) {
            if(player.hasPermissions(2)) {
                writeException(player, t, file);
            }
        }
    }
    public static void writeException(ServerPlayer player, Throwable t) {
        writeException(player, t, null);
    }

    public static void writeException(ServerPlayer player, Throwable t, ResourceLocation file) {
        player.sendSystemMessage(
                Component.literal("AN ERROR HAS OCCURRED!")
                        .setStyle(Style.EMPTY.withBold(true))
                        .withColor(ARGB.color(255, 0, 0))
        );

        System.out.println("An error has occurred!");

        if(t instanceof IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        }
        System.out.println(t.getMessage());
        player.sendSystemMessage(Component.literal(t.getClass().getName()).withColor(ARGB.color(200, 200, 200)));
        player.sendSystemMessage(Component.literal(t.getMessage()).withColor(ARGB.color(200, 200, 200)));

        for(StackTraceElement element : t.getStackTrace()) {
            var className = element.getClassName();
            var methodName = element.getMethodName();

            if(className.contains("dev.akarah.polaris.script.value")) {

                player.sendSystemMessage(Component.empty());
                player.sendSystemMessage(
                        Component.literal("Built-in Call: " + className + "#" + methodName)
                                .withColor(ARGB.color(200, 200, 200))
                );
                continue;
            }

            if(className.contains(CodegenContext.ACTION_CLASS_DESC.displayName())) {
                player.sendSystemMessage(Component.empty());
                var action = CodegenContext.INSTANCE.actions.get(methodName);

                var eventName = action.eventName().orElse(null);
                if(eventName != null) {
                    player.sendSystemMessage(
                            Component.literal("Event: " + eventName)
                                    .withColor(ARGB.color(200, 200, 200))
                    );
                } else {
                    player.sendSystemMessage(
                            Component.literal("User Function: " + action.location())
                                    .withColor(ARGB.color(200, 200, 200))
                    );
                }
                player.sendSystemMessage(
                        Component.literal("@ File " + action.span().fileName() + ", Line" + action.span().debugInfo().line())
                                .withColor(ARGB.color(100, 100, 100))
                );
                player.sendSystemMessage(
                        Component.literal("@ Errored at Line " + element.getLineNumber())
                                .withColor(ARGB.color(100, 100, 100))
                );

            }
        }
    }
}
