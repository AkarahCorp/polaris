package dev.akarah.cdata.script.expr.entity;

import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.script.expr.Expression;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityUtil {
    public static void bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/position"), EntityPositionExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/direction"), EntityDirectionExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/teleport"), EntityTeleportAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/teleport_relative"), EntityTeleportRelativeAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/world"), EntityWorldExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/set_name"), EntitySetNameAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/name"), EntityNameExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/health"), EntityHealthExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/stat"), EntityStatExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/set_attribute"), EntitySetAttributeExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/set_health"), EntitySetHealthExpression.class);
    }

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

    public static Double entityStat(Entity entity, String statKey) {
        if(entity instanceof DynamicEntity entity1) {
            return entity1.base().stats().orElse(StatsObject.EMPTY).get(statKey);
        }
        if(entity instanceof ServerPlayer serverPlayer) {
            return ExtReloadableResources.statManager().lookup(serverPlayer).get(statKey);
        }
        return 0.0;
    }

    public static void setEntityAttribute(Entity entity, ResourceLocation attribute, Double amount) {
        if(entity instanceof LivingEntity le) {
            var attr = BuiltInRegistries.ATTRIBUTE.get(attribute).orElseThrow();
            var instance = le.getAttribute(attr);
            if(instance != null) {
                instance.setBaseValue(amount);
            }
        }
    }

    public static void setHealth(Entity entity, Double health) {
        if(entity instanceof LivingEntity le) {
            le.setHealth(health.floatValue());
        }
    }
}
