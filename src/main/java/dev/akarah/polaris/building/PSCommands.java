package dev.akarah.polaris.building;

import com.mojang.brigadier.CommandDispatcher;
import dev.akarah.polaris.building.palette.Palette;
import dev.akarah.polaris.building.region.Region;
import dev.akarah.polaris.building.wand.WandOperations;
import dev.akarah.polaris.registry.command.LiteralCommandNode;
import dev.akarah.polaris.script.dsl.DslToken;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class PSCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        var root = Commands.literal("ps").requires(x -> x.permissions().hasPermission(Permissions.COMMANDS_ADMIN));
        root.then(Commands.literal("wand").executes(ctx -> {
            try {
                if(ctx.getSource().getEntity() instanceof ServerPlayer player) {
                    player.getInventory().add(WandOperations.emptyWand());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }));

        root.then(Commands.literal("reset").executes(ctx -> {
            WandOperations.applyToWand(
                    ctx.getSource().getEntity(),
                    WandOperations.resetPalettes()
            );
            WandOperations.applyToWand(
                    ctx.getSource().getEntity(),
                    WandOperations.resetRegions()
            );
            return 0;
        }));

        var regRoot = Commands.literal("reg");
        for(var prov : Region.COMMAND_REGISTERS) {
            prov.accept(regRoot, context);
        }


        var palRoot = Commands.literal("pal");
        for(var prov : Palette.COMMAND_REGISTERS) {
            prov.accept(palRoot, context);
        }

        root.then(regRoot);
        root.then(palRoot);

        dispatcher.register(root);
    }
}
