package dev.akarah.cdata.script.expr.vec3;

import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.world.phys.Vec3;

public class Vec3Util {
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
