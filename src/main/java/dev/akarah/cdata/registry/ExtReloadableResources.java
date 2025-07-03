package dev.akarah.cdata.registry;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.akarah.cdata.EngineConfig;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.Util;
import dev.akarah.cdata.registry.codec.MetaCodec;
import dev.akarah.cdata.registry.entity.CustomEntity;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.registry.stat.StatManager;
import dev.akarah.cdata.registry.text.TextElement;
import dev.akarah.cdata.script.dsl.DslActionManager;
import net.minecraft.server.packs.resources.ResourceManager;

import java.nio.file.Files;
import java.nio.file.Paths;
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
    static ReloadableJsonManager<TextElement> TEXT_ELEMENT;

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

    public static ReloadableJsonManager<TextElement> textElement() {
        return TEXT_ELEMENT;
    }

    public static void reloadEverything(ResourceManager resourceManager) {
        ExtReloadableResources.reset();

        try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            ExtReloadableResources.reset();

            // load first, since this is the bottom most dependency of everything tbh
            ExtReloadableResources.textElement().reloadWithManager(resourceManager, executor).get();

            CompletableFuture.allOf(
                    ExtReloadableResources.customItem().reloadWithManager(resourceManager, executor),
                    ExtReloadableResources.actionManager().reloadWithManager(resourceManager, executor),
                    ExtReloadableResources.customEntity().reloadWithManager(resourceManager, executor)
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
        }

        ExtReloadableResources.STAT_MANAGER = new StatManager();
        ExtReloadableResources.ACTION_MANAGER = new DslActionManager();
        ExtReloadableResources.CUSTOM_ITEM = ReloadableJsonManager.of("item", CustomItem.CODEC);
        ExtReloadableResources.CUSTOM_ENTITY = ReloadableJsonManager.of("entity", CustomEntity.CODEC);
        ExtReloadableResources.META_CODEC = ReloadableJsonManager.of("codec", MetaCodec.CODEC);
        ExtReloadableResources.TEXT_ELEMENT = ReloadableJsonManager.of("text", TextElement.CODEC);
    }
}
