package dev.akarah.cdata.script.value.mc;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.db.Database;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import dev.akarah.cdata.registry.entity.VisualEntity;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.*;
import dev.akarah.cdata.script.value.mc.rt.DynamicContainer;
import dev.akarah.cdata.script.value.mc.rt.DynamicContainerMenu;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.*;
import java.util.function.Consumer;

public class RParticle extends RuntimeValue {
    public final ParticleOptions particleOptions;
    public RNumber count = RNumber.of(1.0);

    private RParticle(ParticleOptions particleOptions) {
        this.particleOptions = particleOptions;
    }

    @SuppressWarnings("unchecked")
    @MethodTypeHint(signature = "(particle: particle, color: string) -> particle", documentation = "")
    public static RParticle colored(RParticle particle, RString colorString) {
        var color = Integer.parseInt(colorString.javaValue(), 16);
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
