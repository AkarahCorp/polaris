package dev.akarah.cdata.script.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.text.Parser;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.values.NumberValue;
import dev.akarah.cdata.script.value.values.StringValue;
import dev.akarah.cdata.script.value.values.TextLineValue;

public interface ValueProvider {
    Codec<ValueProvider> CODEC = Codec.lazyInitialized(() ->
            ExtBuiltInRegistries.VALUE_TYPE.byNameCodec().dispatch(ValueProvider::generatorCodec, x -> x));

    Codec<ValueProvider> TEXT_VALUE = Codec.withAlternative(
            Codec.STRING.xmap(Parser::parseTextLine, Record::toString)
                    .xmap(TextLineValue::new, x -> ((TextLineValue) x).line()),
            CODEC
    );

    Codec<ValueProvider> STRING_VALUE = Codec.withAlternative(
            Codec.STRING.xmap(StringValue::new, x -> ((StringValue) x).value()),
            CODEC
    );

    Codec<ValueProvider> NUMBER_CODEC = Codec.withAlternative(
            Codec.DOUBLE.xmap(NumberValue::new, x -> ((NumberValue) x).value()),
            CODEC
    );

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
