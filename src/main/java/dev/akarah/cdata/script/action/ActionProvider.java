package dev.akarah.cdata.script.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.script.env.ScriptContext;

public interface ActionProvider {
    Codec<ActionProvider> CODEC = Codec.lazyInitialized(() ->
            ExtBuiltInRegistries.ACTION_TYPE.byNameCodec().dispatch(ActionProvider::generatorCodec, x -> x));

    void execute(ScriptContext ctx);
    MapCodec<? extends ActionProvider> generatorCodec();
}
