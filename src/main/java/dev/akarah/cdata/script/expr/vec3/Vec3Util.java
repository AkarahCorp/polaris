package dev.akarah.cdata.script.expr.vec3;

import dev.akarah.cdata.script.expr.Expression;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class Vec3Util {
    public static void bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec"), Vec3Expression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vector/add"), Vec3AddExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vector/multiply"), Vec3MultiplyExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vector/x"), Vec3XExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vector/y"), Vec3YExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vector/z"), Vec3ZExpression.class);
    }

    public static Vec3 add(Vec3 lhs, Vec3 rhs) {
        return lhs.add(rhs);
    }

    public static Vec3 mul(Vec3 lhs, Vec3 rhs) {
        return lhs.multiply(rhs);
    }

    public static Double x(Vec3 vec3) {
        return vec3.x;
    }

    public static Double y(Vec3 vec3) {
        return vec3.y;
    }

    public static Double z(Vec3 vec3) {
        return vec3.z;
    }

    public static Vec3 create(Double x, Double y, Double z) {
        return new Vec3(x, y, z);
    }
}
