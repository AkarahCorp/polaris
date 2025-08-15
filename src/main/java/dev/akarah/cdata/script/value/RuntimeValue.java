package dev.akarah.cdata.script.value;

import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.akarah.cdata.script.value.mc.REntity;
import dev.akarah.cdata.script.value.mc.RItem;
import dev.akarah.cdata.script.value.mc.RVector;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

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
                    var struct = ops.getMap(input).map(x -> {
                        var name = ops.getStringValue(x.get("polaris:struct/type")).getOrThrow();

                        var rStruct = RStruct.create(name, (int) (x.entries().count() - 1));
                        return Pair.of(rStruct, input);
                    });
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
        if(this instanceof RNumber number) {
            return number.toString();
        }
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

    @Override
    public boolean equals(Object other) {
        if(this instanceof RItem item1 && other instanceof RItem item2) {
            return RItem.id(item1).equals(RItem.id(item2))
                    && item1.javaValue().getCount() == item2.javaValue().getCount();
        }
        if(other instanceof RuntimeValue runtimeValue) {
            return this.javaValue().equals(runtimeValue.javaValue());
        }
        return this.javaValue().equals(other);
    }

    public static <T> RuntimeValue from(T originalValue, CommandSourceStack stack) {
        return switch (originalValue) {
            case String value -> RString.of(value);
            case Double value -> RNumber.of(value);
            case Boolean value -> RBoolean.of(value);
            case Vec3 value -> RVector.of(value);
            case Entity value -> REntity.of(value);
            case EntitySelector entitySelector -> {
                var list = RList.create();
                try {
                    for(var entity : entitySelector.findEntities(stack)) {
                        RList.add(list, REntity.of(entity));
                    }
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
                yield list;
            }
            default -> RuntimeValue.number(0.0);
        };
    }
}