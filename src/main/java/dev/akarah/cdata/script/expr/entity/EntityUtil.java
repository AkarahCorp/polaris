package dev.akarah.cdata.script.expr.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityUtil {
    public static Vec3 entityDirection(Entity entity) {
        return entity.getLookAngle();
    }

    public static Vec3 entityPosition(Entity entity) {
        return entity.position();
    }

    public static void teleportTo(Entity entity, Vec3 pos) {
        entity.teleportTo(pos.x, pos.y, pos.z);
    }

    public static void teleportRelative(Entity entity, Vec3 pos) {
        entity.teleportRelative(pos.x, pos.y, pos.z);
    }
}
