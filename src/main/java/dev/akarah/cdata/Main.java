package dev.akarah.cdata;

import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.registry.citem.CustomItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class Main implements ModInitializer {
    public static MinecraftServer SERVER;

    @Override
    public void onInitialize() {
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
        });

        System.out.println("Hello world!");
    }
}
