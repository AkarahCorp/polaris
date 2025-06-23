package dev.akarah.cdata.script.env;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Selection {
    List<Entity> entities;

    private Selection(List<Entity> entities) {
        this.entities = entities;
    }

    public static Selection empty() {
        return new Selection(List.of());
    }

    public static Selection of(Entity entity) {
        return new Selection(List.of(entity));
    }

    public static Selection of(Entity... entities) {
        return new Selection(Arrays.asList(entities));
    }

    public static Selection of(List<Entity> entities) {
        return new Selection(entities);
    }

    public void forEach(Consumer<Entity> consumer) {
        entities.forEach(consumer);
    }

    public void forEachPlayer(Consumer<ServerPlayer> consumer) {
        entities.forEach(entity -> {
            if(entity instanceof ServerPlayer serverPlayer) {
                consumer.accept(serverPlayer);
            }
        });
    }

    public int size() {
        return this.entities.size();
    }

    @Override
    public @NotNull String toString() {
        return this.entities
                .stream()
                .map(Entity::getStringUUID)
                .toList()
                .toString();
    }
}
