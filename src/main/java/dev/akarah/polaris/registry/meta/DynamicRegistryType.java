package dev.akarah.polaris.registry.meta;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import dev.akarah.polaris.registry.ReloadableJsonManager;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.RuntimeValue;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public record DynamicRegistryType() {
    public static Codec<DynamicRegistryType> CODEC = Unit.CODEC.xmap(_ -> new DynamicRegistryType(), _ -> Unit.INSTANCE);

    public static void loadEntries(ResourceManager manager, Executor executor) {
        var rootRegistry = Resources.registryTypes().registry();
        Map<Identifier, ReloadableJsonManager<RuntimeValue>> managers = Maps.newHashMap();
        rootRegistry.entrySet().forEach(entry -> {
            var registryId = entry.getKey().identifier();
            var registryMeta = entry.getValue();

            System.out.println("Loading " + "vars/" + registryId.toString().replace(':', '/'));
            var jsonManager = ReloadableJsonManager.of(
                    "vars/" + registryId.toString().replace(':', '/'),
                    RuntimeValue.CODEC
            );
            managers.put(registryId, jsonManager);
        });
        try {
            CompletableFuture.allOf(
                    managers.values().stream()
                            .map(manager1 -> manager1.reloadWithManager(manager, executor))
                            .toArray(CompletableFuture[]::new)
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        managers.forEach((id, jsonManager) -> {
            Resources.dynamicRegistryEntries().put(id, jsonManager.registry());
        });
    }
}
