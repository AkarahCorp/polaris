package dev.akarah.polaris.registry;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ReloadableJsonManager<T> {
    Registry<T> registry;
    String registryName;
    Codec<T> codec;

    private ReloadableJsonManager() {}

    public static <T> ReloadableJsonManager<T> of(String registryName, Codec<T> codec) {
        var rjm = new ReloadableJsonManager<T>();
        rjm.registryName = registryName;
        rjm.codec = codec;
        return rjm;
    }

    public Registry<T> registry() {
        return this.registry;
    }

    public Codec<T> codec() {
        return this.codec;
    }

    public CompletableFuture<Void> reloadWithManager(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture
                .runAsync(
                        () -> {
                            this.registry = new MappedRegistry<>(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("polaris", this.registryName)), Lifecycle.stable());
                            for(var resourceEntry : resourceManager.listResources("engine/" + registryName, rl -> rl.getPath().endsWith(".json")).entrySet()) {
                                try(var inputStream = resourceEntry.getValue().open()) {
                                    var bytes = inputStream.readAllBytes();
                                    var string = new String(bytes);
                                    var json = JsonParser.parseString(string);
                                    var value = codec.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
                                    Registry.register(
                                            this.registry,
                                            resourceEntry.getKey().withPath(s ->
                                                    s.replace(".json", "")
                                                            .replace("engine/" + registryName + "/", "")),
                                            value
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        },
                        executor
                );
    }
}
