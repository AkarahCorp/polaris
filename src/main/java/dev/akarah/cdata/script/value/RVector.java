package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class RVector extends RuntimeValue<Vec3> {
    private final Vec3 inner;

    private RVector(Vec3 inner) {
        this.inner = inner;
    }

    public static RVector of(Vec3 value) {
        return new RVector(value);
    }

    @Override
    public Vec3 javaValue() {
        return this.inner;
    }

    @MethodTypeHint("(lhs: vector, rhs: vector) -> vector")
    public static RVector add(RVector lhs, RVector rhs) {
        return RVector.of(lhs.javaValue().add(rhs.javaValue()));
    }

    @MethodTypeHint("(lhs: vector, rhs: vector) -> vector")
    public static RVector multiply(RVector lhs, RVector rhs) {
        return RVector.of(lhs.javaValue().multiply(rhs.javaValue()));
    }

    public BlockPos asBlockPos() {
        return new BlockPos((int) this.inner.x, (int) this.inner.y, (int) this.inner.z);
    }
}
