package dev.akarah.cdata.script.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.registry.text.Parser;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.values.constants.*;
import dev.akarah.cdata.script.value.values.vec3.Vec3Value;
import net.minecraft.core.Holder;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

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

    Codec<ValueProvider> CUSTOM_ITEM_CODEC = Codec.withAlternative(
            CustomItem.HOLDER_CODEC.xmap(CustomItemValue::new, x -> ((CustomItemValue) x).item()),
            CODEC
    );

    Codec<ValueProvider> VEC3_CODEC = Codec.withAlternative(
            Vec3.CODEC.xmap(
                    a -> new Vec3Value(new NumberValue(a.x), new NumberValue(a.y), new NumberValue(a.z)),
                    x -> (Vec3) x
            ),
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

    default String asString(ScriptContext ctx) {
        var base = this.evaluate(ctx);
        if(base instanceof String s) {
            return s;
        } else {
            return base.toString();
        }
    }

    default double asNumber(ScriptContext ctx) {
        var base = this.evaluate(ctx);
        if(base instanceof Double d) {
            return d;
        } else {
            return 0.0;
        }
    }

    default ParsedText asText(ScriptContext ctx) {
        var base = this.evaluate(ctx);
        if(base instanceof ParsedText p) {
            return p;
        } else {
            return new ParsedText(base.toString(), List.of());
        }
    }

    @SuppressWarnings("unchecked") // checked during runtime
    default Optional<Holder<CustomItem>> asCustomItem(ScriptContext ctx) {
        var base = this.evaluate(ctx);
        if(base instanceof Holder<?> holder
        && holder.value() instanceof CustomItem) {
            return Optional.of((Holder<CustomItem>) holder);
        } else {
            return Optional.empty();
        }
    }

    default Vec3 asVec3(ScriptContext ctx) {
        var base = this.evaluate(ctx);
        if(base instanceof Vec3 vec) {
            return vec;
        } else {
            return Vec3.ZERO;
        }
    }
}
