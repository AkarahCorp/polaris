package dev.akarah.cdata.script.action;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.action.player.AllOfAction;
import dev.akarah.cdata.script.action.player.RepeatTimesAction;
import dev.akarah.cdata.script.action.player.SendActionBarAction;
import dev.akarah.cdata.script.action.player.SendMessageAction;
import net.minecraft.core.Registry;

public class ActionTypes {
    public static MapCodec<? extends ActionProvider> bootStrap(Registry<MapCodec<? extends ActionProvider>> registry) {
        Registry.register(
                registry,
                "player/send_message",
                SendMessageAction.GENERATOR_CODEC
        );

        Registry.register(
                registry,
                "player/send_actionbar",
                SendActionBarAction.GENERATOR_CODEC
        );

        Registry.register(
                registry,
                "all_of",
                AllOfAction.GENERATOR_CODEC
        );

        Registry.register(
                registry,
                "repeat",
                RepeatTimesAction.GENERATOR_CODEC
        );

        return SendMessageAction.GENERATOR_CODEC;
    }
}
