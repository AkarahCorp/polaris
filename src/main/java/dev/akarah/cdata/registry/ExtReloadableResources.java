package dev.akarah.cdata.registry;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.akarah.cdata.EngineConfig;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.Util;
import dev.akarah.cdata.registry.codec.MetaCodec;
import dev.akarah.cdata.registry.entity.CustomEntity;
import dev.akarah.cdata.registry.entity.DynamicEntity;
import dev.akarah.cdata.registry.entity.MobSpawnRule;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.stat.StatManager;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.script.dsl.DslActionManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ExtReloadableResources {
    static StatManager STAT_MANAGER;
    static EngineConfig CONFIG;
    static DslActionManager ACTION_MANAGER;
    static ReloadableJsonManager<CustomItem> CUSTOM_ITEM;
    static ReloadableJsonManager<CustomEntity> CUSTOM_ENTITY;
    static ReloadableJsonManager<MetaCodec<?>> META_CODEC;
    static ReloadableJsonManager<MobSpawnRule> MOB_SPAWN_RULE;

    public static StatManager statManager() {
        return STAT_MANAGER;
    }

    public static DslActionManager actionManager() {
        return ACTION_MANAGER;
    }

    public static EngineConfig config() {
        return CONFIG;
    }

    public static ReloadableJsonManager<CustomItem> customItem() {
        return CUSTOM_ITEM;
    }

    public static ReloadableJsonManager<CustomEntity> customEntity() {
        return CUSTOM_ENTITY;
    }

    public static ReloadableJsonManager<MobSpawnRule> mobSpawnRule() {
        return MOB_SPAWN_RULE;
    }

    public static void reloadEverything(ResourceManager resourceManager) {
        ExtReloadableResources.reset();

        try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture.allOf(
                    ExtReloadableResources.customItem().reloadWithManager(resourceManager, executor),
                    ExtReloadableResources.actionManager().reloadWithManager(resourceManager, executor),
                    ExtReloadableResources.customEntity().reloadWithManager(resourceManager, executor),
                    ExtReloadableResources.mobSpawnRule().reloadWithManager(resourceManager, executor)
            ).get();
        } catch (ExecutionException | InterruptedException e) {
            Main.handleError(e);
        }
    }

    public static void reset() {
        var engineConfigPath = Paths.get("./engine.json");
        if(Files.exists(engineConfigPath)) {
            var json = JsonParser.parseString(Util.sneakyThrows(() -> Files.readString(engineConfigPath)));
            ExtReloadableResources.CONFIG = EngineConfig.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
        } else {
            Util.sneakyThrows(() -> {
                Files.createFile(engineConfigPath);
                ExtReloadableResources.CONFIG = new EngineConfig(StatsObject.of());
                var json = EngineConfig.CODEC.encodeStart(JsonOps.INSTANCE, ExtReloadableResources.CONFIG).getOrThrow();
                Files.writeString(engineConfigPath, json.toString());
                return null;
            });
        }

        ExtReloadableResources.STAT_MANAGER = new StatManager();
        ExtReloadableResources.ACTION_MANAGER = new DslActionManager();
        ExtReloadableResources.CUSTOM_ITEM = ReloadableJsonManager.of("item", CustomItem.CODEC);
        ExtReloadableResources.CUSTOM_ENTITY = ReloadableJsonManager.of("entity", CustomEntity.CODEC);
        ExtReloadableResources.META_CODEC = ReloadableJsonManager.of("codec", MetaCodec.CODEC);
        ExtReloadableResources.MOB_SPAWN_RULE = ReloadableJsonManager.of("rule/mob_spawn", MobSpawnRule.CODEC);
    }
}
