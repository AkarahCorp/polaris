package dev.akarah.cdata.script.value;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.akarah.cdata.script.value.mc.RVector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public abstract class RuntimeValue {
    public abstract Object javaValue();

    public static Codec<RuntimeValue> CODEC = Codec.lazyInitialized(() -> Codec.recursive(
            "RuntimeValue",
            runtimeValueCodec -> new Codec<>() {
                @Override
                public <T> DataResult<Pair<RuntimeValue, T>> decode(DynamicOps<T> ops, T input) {
                    var str = ops.getStringValue(input).map(x -> Pair.of((RuntimeValue) RString.of(x), input));
                    if(str.isSuccess()) {
                        return str;
                    }
                    var num = ops.getNumberValue(input).map(x -> Pair.of((RuntimeValue) RNumber.of(x.doubleValue()), input));
                    if(num.isSuccess()) {
                        return num;
                    }
                    var bool = ops.getBooleanValue(input).map(x -> Pair.of((RuntimeValue) RBoolean.of(x), input));
                    if(bool.isSuccess()) {
                        return num;
                    }
                    return DataResult.error(() -> "Expected a string, number, or boolean.");
                }

                @Override
                public <T> DataResult<T> encode(RuntimeValue input, DynamicOps<T> ops, T prefix) {
                    return switch (input) {
                        case RString string -> DataResult.success(ops.createString(string.javaValue()));
                        case RNumber number -> DataResult.success(ops.createNumeric(number.javaValue()));
                        case RBoolean bool -> DataResult.success(ops.createBoolean(bool.javaValue()));
                        default -> DataResult.error(() -> "Expected a string, number, or boolean.");
                    };
                }
            }
    ));

    public static RDict dict() {
        return RDict.create();
    }

    public static RList list() {
        return RList.create();
    }

    public static RNumber number(double value) {
        return RNumber.of(value);
    }

    @Override
    public String toString() {
        return this.javaValue().toString();
    }

    @Override
    public int hashCode() {
        return this.javaValue().hashCode();
    }

    @SafeVarargs
    private static <T> Codec<T> alternatives(Codec<T>... codecs) {
        var c = codecs[0];
        for(int i = 0; i < codecs.length; i++) {
            c = Codec.either(c, codecs[i]).xmap(Either::unwrap, Either::left);
        }
        return c;
    }
}