package dev.akarah.cdata.script.action;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.action.env.SetLocalAction;
import dev.akarah.cdata.script.action.player.*;
import net.minecraft.core.Registry;

public class ActionTypes {
    public static MapCodec<? extends ActionProvider> bootStrap(Registry<MapCodec<? extends ActionProvider>> registry) {
        Registry.register(registry, "player/send_message", SendMessageAction.GENERATOR_CODEC);
        Registry.register(registry, "player/send_actionbar", SendActionBarAction.GENERATOR_CODEC);
        Registry.register(registry, "player/give_item", GiveItemAction.GENERATOR_CODEC);
        Registry.register(registry, "entity/teleport", TeleportAction.GENERATOR_CODEC);
        Registry.register(registry, "all_of", AllOfAction.GENERATOR_CODEC);
        Registry.register(registry, "repeat", RepeatTimesAction.GENERATOR_CODEC);
        Registry.register(registry, "local/store", SetLocalAction.GENERATOR_CODEC);
        return SendMessageAction.GENERATOR_CODEC;
    }
}
