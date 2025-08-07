package dev.akarah.cdata.registry;

import com.google.common.collect.Maps;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import dev.akarah.cdata.config.EngineConfig;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.Scheduler;
import dev.akarah.cdata.Util;
import dev.akarah.cdata.db.persistence.DbPersistence;
import dev.akarah.cdata.registry.command.CommandBuilderNode;
import dev.akarah.cdata.registry.entity.CustomEntity;
import dev.akarah.cdata.registry.entity.MobSpawnRule;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.mining.MiningManager;
import dev.akarah.cdata.registry.mining.MiningRule;
import dev.akarah.cdata.registry.refreshable.Refreshable;
import dev.akarah.cdata.registry.stat.StatManager;
import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.script.dsl.DslActionManager;
import net.minecraft.server.packs.resources.ResourceManager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Resources {
    static StatManager STAT_MANAGER;
    static EngineConfig CONFIG;
    static DslActionManager ACTION_MANAGER;
    static MiningManager MINING_MANAGER;
    static Scheduler SCHEDULER = new Scheduler();
    static ReloadableJsonManager<CustomItem> CUSTOM_ITEM;
    static ReloadableJsonManager<CustomEntity> CUSTOM_ENTITY;
    static ReloadableJsonManager<MobSpawnRule> MOB_SPAWN_RULE;
    static ReloadableJsonManager<MiningRule> MINING_RULE;
    static ReloadableJsonManager<Refreshable> REFRESHABLES;
    static ReloadableJsonManager<CommandBuilderNode> COMMAND_NODES;
    public static Map<UUID, GameProfile> GAME_PROFILES = Maps.newHashMap();
    public static boolean addedGameProfiles = false;

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

    public static Scheduler scheduler() {
        return SCHEDULER;
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

    public static ReloadableJsonManager<Refreshable> refreshable() {
        return REFRESHABLES;
    }

    public static ReloadableJsonManager<CommandBuilderNode> command() {
        return COMMAND_NODES;
    }

    public static void reloadEverything(ResourceManager resourceManager) {

        try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Resources.reset();
            CompletableFuture.allOf(
                    Resources.actionManager().reloadWithManager(resourceManager, executor),
                    Resources.customItem().reloadWithManager(resourceManager, executor),
                    Resources.customEntity().reloadWithManager(resourceManager, executor),
                    Resources.mobSpawnRule().reloadWithManager(resourceManager, executor),
                    Resources.miningRule().reloadWithManager(resourceManager, executor),
                    Resources.refreshable().reloadWithManager(resourceManager, executor),
                    Resources.command().reloadWithManager(resourceManager, executor)
            ).get();

            Resources.statManager().refreshPlayerInventories();
        } catch (Exception e) {
            Main.handleError(e);
        }
    }

    public static void reset() {
        Resources.addedGameProfiles = false;
        Resources.GAME_PROFILES.clear();
        var engineConfigPath = Paths.get("./engine.json");
        if(Files.exists(engineConfigPath)) {
            var json = JsonParser.parseString(Util.sneakyThrows(() -> Files.readString(engineConfigPath)));
            Resources.CONFIG = EngineConfig.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
        } else {
            Util.sneakyThrows(() -> {
                Files.createFile(engineConfigPath);
                Resources.CONFIG = new EngineConfig(StatsObject.of(), Optional.empty());
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
        Resources.MOB_SPAWN_RULE = ReloadableJsonManager.of("rule/mob_spawn", MobSpawnRule.CODEC);
        Resources.MINING_RULE = ReloadableJsonManager.of("rule/mining", MiningRule.CODEC);
        Resources.REFRESHABLES = ReloadableJsonManager.of("rule/placement", Refreshable.CODEC);
        Resources.COMMAND_NODES = ReloadableJsonManager.of("command", CommandBuilderNode.CODEC);
    }
}
