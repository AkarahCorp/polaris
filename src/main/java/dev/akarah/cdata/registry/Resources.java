package dev.akarah.cdata.registry;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.akarah.cdata.EngineConfig;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.Util;
import dev.akarah.cdata.registry.codec.MetaCodec;
import dev.akarah.cdata.registry.entity.CustomEntity;
import dev.akarah.cdata.registry.entity.MobSpawnRule;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.mining.MiningManager;
import dev.akarah.cdata.registry.mining.MiningRule;
import dev.akarah.cdata.registry.stat.StatManager;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.script.dsl.DslActionManager;
import net.minecraft.server.packs.resources.ResourceManager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Resources {
    static StatManager STAT_MANAGER;
    static EngineConfig CONFIG;
    static DslActionManager ACTION_MANAGER;
    static MiningManager MINING_MANAGER;
    static ReloadableJsonManager<CustomItem> CUSTOM_ITEM;
    static ReloadableJsonManager<CustomEntity> CUSTOM_ENTITY;
    static ReloadableJsonManager<MetaCodec<?>> META_CODEC;
    static ReloadableJsonManager<MobSpawnRule> MOB_SPAWN_RULE;
    static ReloadableJsonManager<MiningRule> MINING_RULE;

    public static StatManager statManager() {
        return STAT_MANAGER;
    }

    public static DslActionManager actionManager() {
        return ACTION_MANAGER;
    }

    public static MiningManager miningManager() {
        return MINING_MANAGER;
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

    public static ReloadableJsonManager<MiningRule> miningRule() {
        return MINING_RULE;
    }

    public static void reloadEverything(ResourceManager resourceManager) {
        Resources.reset();

        try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture.allOf(
                    Resources.customItem().reloadWithManager(resourceManager, executor),
                    Resources.actionManager().reloadWithManager(resourceManager, executor),
                    Resources.customEntity().reloadWithManager(resourceManager, executor),
                    Resources.mobSpawnRule().reloadWithManager(resourceManager, executor),
                    Resources.miningRule().reloadWithManager(resourceManager, executor)
            ).get();
        } catch (ExecutionException | InterruptedException e) {
            Main.handleError(e);
        }
    }

    public static void reset() {
        var engineConfigPath = Paths.get("./engine.json");
        if(Files.exists(engineConfigPath)) {
            var json = JsonParser.parseString(Util.sneakyThrows(() -> Files.readString(engineConfigPath)));
            Resources.CONFIG = EngineConfig.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
        } else {
            Util.sneakyThrows(() -> {
                Files.createFile(engineConfigPath);
                Resources.CONFIG = new EngineConfig(StatsObject.of());
                var json = EngineConfig.CODEC.encodeStart(JsonOps.INSTANCE, Resources.CONFIG).getOrThrow();
                Files.writeString(engineConfigPath, json.toString());
                return null;
            });
        }

        Resources.STAT_MANAGER = new StatManager();
        Resources.ACTION_MANAGER = new DslActionManager();
        Resources.MINING_MANAGER = new MiningManager();
        Resources.CUSTOM_ITEM = ReloadableJsonManager.of("item", CustomItem.CODEC);
        Resources.CUSTOM_ENTITY = ReloadableJsonManager.of("entity", CustomEntity.CODEC);
        Resources.META_CODEC = ReloadableJsonManager.of("codec", MetaCodec.CODEC);
        Resources.MOB_SPAWN_RULE = ReloadableJsonManager.of("rule/mob_spawn", MobSpawnRule.CODEC);
        Resources.MINING_RULE = ReloadableJsonManager.of("rule/mining", MiningRule.CODEC);
    }
}
