package dev.akarah.cdata.script.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.script.env.ScriptContext;

public interface ValueProvider {
    Codec<ValueProvider> CODEC = Codec.lazyInitialized(() ->
            ExtBuiltInRegistries.VALUE_TYPE.byNameCodec().dispatch(ValueProvider::generatorCodec, x -> x));

    Object evaluate(ScriptContext ctx);
    MapCodec<? extends ValueProvider> generatorCodec();

    default <T> T evaluate(ScriptContext ctx, Class<T> clazz) {
        var base = this.evaluate(ctx);
        if(clazz.isInstance(base)) {
            return clazz.cast(base);
        }
        throw new RuntimeException("Expected " + clazz.getName() + ", found " + base.getClass().getName());
    }
}
