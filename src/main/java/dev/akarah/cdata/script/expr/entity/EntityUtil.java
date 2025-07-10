package dev.akarah.cdata.script.expr.entity;

import dev.akarah.cdata.registry.entity.DynamicEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityUtil {
    public static Vec3 entityDirection(Entity entity) {
        return entity.getLookAngle();
    }

    public static Vec3 entityPosition(Entity entity) {
        return entity.position();
    }

    public static Level entityWorld(Entity entity) {
        return entity.level();
    }

    public static Double health(Entity entity) {
        if(entity instanceof LivingEntity le) {
            return (double) le.getHealth();
        }
        return -1.0;
    }

    public static void teleportTo(Entity entity, Vec3 pos) {
        entity.teleportTo(pos.x, pos.y, pos.z);
    }

    public static void teleportRelative(Entity entity, Vec3 pos) {
        entity.teleportRelative(pos.x, pos.y, pos.z);
    }

    public static String entityName(Entity entity) {
        if(entity instanceof DynamicEntity dynamicEntity) {
            return dynamicEntity.base().name();
        }
        return entity.getDisplayName().getString();
    }

    public static void setEntityName(Entity entity, Component name) {
        entity.setCustomName(name);
    }
}
