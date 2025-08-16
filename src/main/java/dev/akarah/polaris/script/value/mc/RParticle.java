package dev.akarah.polaris.script.value.mc;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.value.*;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class RParticle extends RuntimeValue {
    public final ParticleOptions particleOptions;
    public RNumber count = RNumber.of(1.0);

    private RParticle(ParticleOptions particleOptions) {
        this.particleOptions = particleOptions;
    }

    @SuppressWarnings("unchecked")
    @MethodTypeHint(signature = "(particle: particle, color: string) -> particle",
                    documentation = "Colors the particle with an [A]RGB hexadecimal string.")
    public static RParticle colored(RParticle particle, RString colorString) {
        var str = colorString.javaValue();
        if (str.startsWith("#")) {str = str.substring(1);}
        var color = Integer.parseInt(str, 16);
        return new RParticle(ColorParticleOption.create(
                (ParticleType<ColorParticleOption>) particle.particleOptions().getType(),
                color)
        );
    }

    public ParticleOptions particleOptions() {
        return this.particleOptions;
    }

    public static RParticle of(ParticleOptions particleOptions) {
        return new RParticle(particleOptions);
    }

    @Override
    public RParticle javaValue() {
        return this;
    }

    public static Map<ResourceLocation, ParticleOptions> OPTIONS = new HashMap<>();

    static {
        for(var entry : BuiltInRegistries.PARTICLE_TYPE.entrySet()) {
            if(entry.getValue() instanceof SimpleParticleType simpleParticleType) {
                OPTIONS.put(entry.getKey().location(), simpleParticleType);
            }
        }
    }
}
