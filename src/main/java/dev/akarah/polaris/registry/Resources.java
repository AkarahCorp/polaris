package dev.akarah.polaris.registry;

import com.google.common.collect.Maps;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.akarah.polaris.config.EngineConfig;
import dev.akarah.polaris.Main;
import dev.akarah.polaris.Scheduler;
import dev.akarah.polaris.Util;
import dev.akarah.polaris.registry.achievement.AchievementCriteriaType;
import dev.akarah.polaris.registry.achievement.AchievementKind;
import dev.akarah.polaris.registry.achievement.AchievementTrigger;
import dev.akarah.polaris.registry.command.CommandBuilderNode;
import dev.akarah.polaris.registry.effect.CustomEffect;
import dev.akarah.polaris.registry.effect.EffectManager;
import dev.akarah.polaris.registry.entity.CustomEntity;
import dev.akarah.polaris.registry.entity.MobSpawnRule;
import dev.akarah.polaris.registry.item.CustomItem;
import dev.akarah.polaris.registry.meta.DynamicRegistryType;
import dev.akarah.polaris.registry.mining.MiningManager;
import dev.akarah.polaris.registry.mining.MiningRule;
import dev.akarah.polaris.registry.refreshable.Refreshable;
import dev.akarah.polaris.registry.stat.StatManager;
import dev.akarah.polaris.registry.stat.StatType;
import dev.akarah.polaris.registry.stat.StatsObject;
import dev.akarah.polaris.script.dsl.DslActionManager;
import dev.akarah.polaris.script.value.RuntimeValue;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class Resources {
    static StatManager STAT_MANAGER;
    static EffectManager EFFECT_MANAGER;
    static EngineConfig CONFIG;
    static DslActionManager ACTION_MANAGER;
    static MiningManager MINING_MANAGER;
    static Scheduler SCHEDULER = new Scheduler();
    static ReloadableJsonManager<CustomItem> CUSTOM_ITEM;
    static ReloadableJsonManager<CustomEntity> CUSTOM_ENTITY;
    static ReloadableJsonManager<CustomEffect> CUSTOM_EFFECT;
    static ReloadableJsonManager<MobSpawnRule> MOB_SPAWN_RULE;
    static ReloadableJsonManager<MiningRule> MINING_RULE;
    static ReloadableJsonManager<Refreshable> REFRESHABLES;
    static ReloadableJsonManager<CommandBuilderNode> COMMAND_NODES;
    static ReloadableJsonManager<StatType> STAT_TYPE;
    static ReloadableJsonManager<DynamicRegistryType> REGISTRY_TYPE;
    public static ReloadableJsonManager<AchievementKind> ACHIEVEMENT_KIND;
    public static ReloadableJsonManager<AchievementTrigger> ACHIEVEMENT_TRIGGER;
    public static ReloadableJsonManager<AchievementCriteriaType> ACHIEVEMENT_CRITERIA;

    static Map<ResourceLocation, Registry<RuntimeValue>> DYNAMIC_REGISTRY_ENTRIES = Maps.newConcurrentMap();

    public static StatManager statManager() {
        return STAT_MANAGER;
    }

    public static DslActionManager actionManager() {
        return ACTION_MANAGER;
    }

    public static EffectManager effectManager() {
        return EFFECT_MANAGER;
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

    public static ReloadableJsonManager<CustomEffect> customEffect() {
        return CUSTOM_EFFECT;
    }
  
    public static ReloadableJsonManager<StatType> statType() {
        return STAT_TYPE;
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

    public static ReloadableJsonManager<DynamicRegistryType> registryTypes() { return REGISTRY_TYPE; }

    public static Map<ResourceLocation, Registry<RuntimeValue>> dynamicRegistryEntries() {
        return DYNAMIC_REGISTRY_ENTRIES;
    }

    public static void reloadEverything(ResourceManager resourceManager) {
        System.out.println(resourceManager.getClass());
        try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Resources.reset();
            CompletableFuture.allOf(
                    Resources.actionManager().reloadWithManager(resourceManager, executor),
                    Resources.customItem().reloadWithManager(resourceManager, executor),
                    Resources.customEntity().reloadWithManager(resourceManager, executor),
                    Resources.customEffect().reloadWithManager(resourceManager, executor),
                    Resources.mobSpawnRule().reloadWithManager(resourceManager, executor),
                    Resources.miningRule().reloadWithManager(resourceManager, executor),
                    Resources.refreshable().reloadWithManager(resourceManager, executor),
                    Resources.command().reloadWithManager(resourceManager, executor),
                    Resources.statType().reloadWithManager(resourceManager, executor),
                    Resources.registryTypes().reloadWithManager(resourceManager, executor),
                    Resources.ACHIEVEMENT_CRITERIA.reloadWithManager(resourceManager, executor),
                    Resources.ACHIEVEMENT_TRIGGER.reloadWithManager(resourceManager, executor),
                    Resources.ACHIEVEMENT_KIND.reloadWithManager(resourceManager, executor)
            ).get();

            tempCreateAdvancements();

            DynamicRegistryType.loadEntries(resourceManager, executor);

            Resources.statManager().refreshPlayerInventories();
        } catch (Exception e) {
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
                Resources.CONFIG = new EngineConfig(StatsObject.of(), Optional.empty(), "undefined");
                var json = EngineConfig.CODEC.encodeStart(JsonOps.INSTANCE, Resources.CONFIG).getOrThrow();
                Files.writeString(engineConfigPath, json.toString());
                return null;
            });
        }

        Resources.STAT_MANAGER = new StatManager();
        Resources.EFFECT_MANAGER = new EffectManager();
        Resources.ACTION_MANAGER = new DslActionManager();
        Resources.MINING_MANAGER = new MiningManager();
        Resources.CUSTOM_ITEM = ReloadableJsonManager.of("engine/item", CustomItem.CODEC);
        Resources.CUSTOM_ENTITY = ReloadableJsonManager.of("engine/entity", CustomEntity.CODEC);
        Resources.CUSTOM_EFFECT = ReloadableJsonManager.of("engine/effect", CustomEffect.CODEC);
        Resources.MOB_SPAWN_RULE = ReloadableJsonManager.of("engine/rule/mob_spawn", MobSpawnRule.CODEC);
        Resources.MINING_RULE = ReloadableJsonManager.of("engine/rule/mining", MiningRule.CODEC);
        Resources.REFRESHABLES = ReloadableJsonManager.of("engine/rule/placement", Refreshable.CODEC);
        Resources.COMMAND_NODES = ReloadableJsonManager.of("engine/command", CommandBuilderNode.CODEC);
        Resources.STAT_TYPE = ReloadableJsonManager.of("engine/stat", StatType.CODEC);
        Resources.REGISTRY_TYPE = ReloadableJsonManager.of("engine/registry", DynamicRegistryType.CODEC);
        Resources.ACHIEVEMENT_CRITERIA = ReloadableJsonManager.of("engine/achievement/criteria", AchievementCriteriaType.CODEC);
        Resources.ACHIEVEMENT_TRIGGER = ReloadableJsonManager.of("engine/achievement/trigger", AchievementTrigger.CODEC);
        Resources.ACHIEVEMENT_KIND = ReloadableJsonManager.of("engine/achievement/kind", AchievementKind.CODEC);
    }

    public static void loopPlayers() {
        var server = Main.server();
        effectManager().tickPlayers();
        statManager().tickPlayers();
        miningManager().tickPlayers();
        if(server.getTickCount() % 200 == 0) {
            Resources.statManager().refreshPlayerInventories();
        }

        Resources.actionManager().performEvents("server.tick");

        for(var refreshable : Resources.refreshable().registry().entrySet()) {
            refreshable.getValue().execute();
        }

        Resources.mobSpawnRule().registry().listElements().forEach(rule -> rule.value().tick());
    }

    public static void tempCreateAdvancements() {
        var p = Paths.get("./world/datapacks/polaris-tmp/");

        try {

            try {
                var meta = Files.createFile(p.resolve("./pack.mcmeta"));
                Files.writeString(meta, """
                        {
                          "pack": {
                            "description": "polaris auto-generated datapack",
                            "pack_format": 80
                          }
                        }
                        """);
            } catch (IOException e) {

            }

            Files.createDirectories(p);
            for(var adv : Resources.ACHIEVEMENT_KIND.registry().entrySet()) {
                var f = p.resolve("./data/" + adv.getKey().location().getNamespace() + "/advancement/" + adv.getKey().location().getPath() + ".json");
                try {
                    Files.createDirectories(f.getParent());
                    Files.createFile(f);
                } catch (Exception e) {
                }
                Files.writeString(
                        f,
                        """
                                {
                                    "criteria": {
                                        "impossible": {
                                            "trigger": "impossible"
                                        }
                                    },
                                    "display": {
                                        "icon": {
                                            "id": "{icon}"
                                        },
                                        "description": "{description}",
                                        "title": "{name}",
                                        "hidden": false
                                    },
                                    "parent": "{parent}"
                                }
                                """
                                .replace("{description}", adv.getValue().description())
                                .replace("{name}", adv.getValue().name())
                                .replace("{parent}", adv.getValue().parent().toString())
                                .replace("{icon}", adv.getValue().icon().toString())
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void tempDeleteAdvancements() {
        var p = Paths.get("./world/datapacks/polaris-tmp/");
        try {
            Files.deleteIfExists(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
