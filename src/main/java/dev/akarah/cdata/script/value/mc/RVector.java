package dev.akarah.cdata.script.value.mc;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.RNumber;
import dev.akarah.cdata.script.value.RuntimeValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class RVector extends RuntimeValue {
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

    @MethodTypeHint(signature = "(lhs: vector, rhs: vector) -> vector", documentation = "Adds two vectors together, returning a new one with the sum.")
    public static RVector add(RVector lhs, RVector rhs) {
        return RVector.of(lhs.javaValue().add(rhs.javaValue()));
    }

    @MethodTypeHint(signature = "(lhs: vector, rhs: vector) -> vector", documentation = "Multiplies two vectors together element-wise, returning a new vector.")
    public static RVector multiply(RVector lhs, RVector rhs) {
        return RVector.of(lhs.javaValue().multiply(rhs.javaValue()));
    }

    @MethodTypeHint(signature = "(v: vector) -> number", documentation = "Gets the X component of this vector.")
    public static RNumber x(RVector vector) {
        return RNumber.of(vector.javaValue().x);
    }

    @MethodTypeHint(signature = "(v: vector) -> number", documentation = "Gets the Y component of this vector.")
    public static RNumber y(RVector vector) {
        return RNumber.of(vector.javaValue().y);
    }

    @MethodTypeHint(signature = "(v: vector) -> number", documentation = "Gets the Z component of this vector.")
    public static RNumber z(RVector vector) {
        return RNumber.of(vector.javaValue().z);
    }

    @MethodTypeHint(signature = "(vec: vector, value: number) -> vector", documentation = "Returns a new copy of this vector, with the new X component specified.")
    public static RVector with_x(RVector vector, RNumber value) {
        return RVector.of(vector.javaValue().with(Direction.Axis.X, value.doubleValue()));
    }

    @MethodTypeHint(signature = "(vec: vector, value: number) -> vector", documentation = "Returns a new copy of this vector, with the new Y component specified.")
    public static RVector with_y(RVector vector, RNumber value) {
        return RVector.of(vector.javaValue().with(Direction.Axis.Y, value.doubleValue()));
    }

    @MethodTypeHint(signature = "(vec: vector, value: number) -> vector", documentation = "Returns a new copy of this vector, with the new Z component specified.")
    public static RVector with_z(RVector vector, RNumber value) {
        return RVector.of(vector.javaValue().with(Direction.Axis.Z, value.doubleValue()));
    }

    @MethodTypeHint(signature = "(lhs: vector, rhs: vector) -> number", documentation = "Returns the distance between this and another vector.")
    public static RNumber distance(RVector lhs, RVector rhs) {
        return RNumber.of(lhs.javaValue().distanceTo(rhs.javaValue()));
    }

    public BlockPos asBlockPos() {
        return new BlockPos((int) Math.floor(this.inner.x), (int) Math.floor(this.inner.y), (int) Math.floor(this.inner.z));
    }
}
