package dev.akarah.polaris.script.value.mc;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.value.RBoolean;
import dev.akarah.polaris.script.value.RNumber;
import dev.akarah.polaris.script.value.RuntimeValue;
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

    @MethodTypeHint(signature = "(lhs: vector, rhs: vector) -> vector", documentation = "Subtracts two vectors together, returning a new one with the difference.")
    public static RVector sub(RVector lhs, RVector rhs) {
        return RVector.of(lhs.javaValue().subtract(rhs.javaValue()));
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

    @MethodTypeHint(signature = "(vec: vector) -> vector", documentation = "Returns a normalized copy of this vector.")
    public static RVector normalize(RVector vector) {
        return RVector.of(vector.javaValue().normalize());
    }

    @MethodTypeHint(signature = "(vec: vector) -> vector", documentation = "Centers all values in the vector.")
    public static RVector center(RVector vector) {
        return RVector.of(vector.asBlockPos().getCenter());
    }

    @MethodTypeHint(signature = "(lhs: vector, rhs: vector) -> number", documentation = "Returns the distance between this and another vector.")
    public static RNumber distance(RVector lhs, RVector rhs) {
        return RNumber.of(lhs.javaValue().distanceTo(rhs.javaValue()));
    }

    @MethodTypeHint(signature = "(vec: vector, corner1: vector, corner2: vector) -> boolean", documentation = "Returns whether the provided vector is in between the two other vectors.")
    public static RBoolean is_within(RVector vector, RVector c1, RVector c2) {
        var vo = vector.javaValue();
        var v1 = c1.javaValue();;
        var v2 = c2.javaValue();

        return RBoolean.of(
                ((v1.x < v2.x) ? (vo.x >= v1.x && vo.x <= v2.x) : (vo.x <= v1.x && vo.x >= v2.x))
                && ((v1.y < v2.y) ? (vo.y >= v1.y && vo.y <= v2.y) : (vo.y <= v1.y && vo.y >= v2.y))
                && ((v1.z < v2.z) ? (vo.z >= v1.z && vo.z <= v2.z) : (vo.z <= v1.z && vo.z >= v2.z))
        );
    }

    public BlockPos asBlockPos() {
        return new BlockPos((int) Math.floor(this.inner.x), (int) Math.floor(this.inner.y), (int) Math.floor(this.inner.z));
    }
}
