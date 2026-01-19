package dev.akarah.polaris.script.value.mc;

import com.mojang.datafixers.util.Pair;
import dev.akarah.polaris.registry.item.CustomItem;
import dev.akarah.polaris.registry.stat.StatsObject;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.value.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.Objects;
import java.util.Optional;

public class RItemMeta extends RuntimeValue {
    private final CustomItem inner;

    private RItemMeta(CustomItem inner) {
        this.inner = inner;
    }

    public static RItemMeta of(CustomItem value) {
        return new RItemMeta(value);
    }


    public static String typeName() {
        return "item_meta";
    }

    @Override
    public CustomItem javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return RItemMeta.of(this.inner);
    }
}
