package dev.akarah.cdata;

import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.registry.citem.CustomItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

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

            dispatcher.register(root);
        });

        System.out.println("Hello world!");
    }
}
