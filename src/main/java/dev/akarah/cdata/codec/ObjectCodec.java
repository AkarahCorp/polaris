package dev.akarah.cdata.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record ObjectCodec(Map<String, MetaCodec<?>> map) implements MetaCodec<Map<String, Object>> {
    public static MapCodec<ObjectCodec> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, MetaCodec.DIRECT_CODEC).fieldOf("fields").forGetter(ObjectCodec::map)
    ).apply(instance, ObjectCodec::new));

    @Override
    public Codec<Map<String, Object>> codec() {
        return new ObjectCodecResult(this);
    }

    @Override
    public MapCodec<? extends MetaCodec<Map<String, Object>>> generatorCodec() {
        return GENERATOR_CODEC;
    }

    record ObjectCodecResult(ObjectCodec objectCodec) implements Codec<Map<String, Object>> {
        @Override
        public <T> DataResult<Pair<Map<String, Object>, T>> decode(DynamicOps<T> ops, T input) {
            try {
                var map = new HashMap<String, Object>();
                var object = ops.getMap(input).getOrThrow();
                for(var entry : objectCodec.map.entrySet()) {
                    var valueToBe = object.get(entry.getKey());
                    if(valueToBe == null) {
                        throw new RuntimeException("Expected key '" + entry.getKey() + "'");
                    }

                    var encodedValue = entry.getValue().codec().decode(ops, valueToBe);
                    try {
                        map.put(entry.getKey(), encodedValue.getOrThrow());
                    } catch (Exception e) {
                        throw new RuntimeException("Key '" + entry.getKey() + "' must match " + entry.getValue());
                    }
                }
                return DataResult.success(new Pair<>(map, input));
            } catch (Exception e) {
                return DataResult.error(e::getMessage);
            }
        }

        @Override
        public <T> DataResult<T> encode(Map<String, Object> input, DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "ObjectCodec can not be encoded currently.");
        }
    }

    @Override
    public @NotNull String toString() {
        return "object" + this.map;
    }
}
