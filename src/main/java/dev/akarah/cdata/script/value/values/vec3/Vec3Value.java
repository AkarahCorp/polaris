package dev.akarah.cdata.script.value.values.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.value.ValueProvider;
import net.minecraft.world.phys.Vec3;

public record Vec3Value(
        ValueProvider x,
        ValueProvider y,
        ValueProvider z
) implements ValueProvider {
    public static MapCodec<Vec3Value> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ValueProvider.NUMBER_CODEC.fieldOf("x").forGetter(Vec3Value::x),
            ValueProvider.NUMBER_CODEC.fieldOf("y").forGetter(Vec3Value::y),
            ValueProvider.NUMBER_CODEC.fieldOf("z").forGetter(Vec3Value::z)
    ).apply(instance, Vec3Value::new));

    @Override
    public Object evaluate(ScriptContext ctx) {
        return new Vec3(
                this.x.asNumber(ctx),
                this.y.asNumber(ctx),
                this.z.asNumber(ctx)
        );
    }

    @Override
    public MapCodec<? extends ValueProvider> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
