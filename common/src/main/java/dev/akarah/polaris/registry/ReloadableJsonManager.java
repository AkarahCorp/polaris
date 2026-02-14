package dev.akarah.polaris.registry;

import com.google.common.collect.Maps;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ReloadableJsonManager<T> {
    Registry<T> registry;
    String registryName;
    Codec<T> codec;
    Map<Identifier, Identifier> remapIds = Maps.newHashMap();

    private ReloadableJsonManager() {}

    public static <T> ReloadableJsonManager<T> of(String registryName, Codec<T> codec) {
        var rjm = new ReloadableJsonManager<T>();
        rjm.registryName = registryName;
        rjm.codec = codec;
        return rjm;
    }

    public Registry<T> registry() {
        return new RegistryWrapper<>(this.registry, this.remapIds);
    }

    public Codec<T> codec() {
        return this.codec;
    }

    public CompletableFuture<Void> reloadWithManager(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture
                .runAsync(
                        () -> {

                            // insert values into our registry
                            this.registry = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("polaris", this.registryName)), Lifecycle.stable());
                            for(var resourceEntry : resourceManager.listResources(registryName, rl -> rl.getPath().endsWith(".json")).entrySet()) {
                                try(var inputStream = resourceEntry.getValue().open()) {
                                    var bytes = inputStream.readAllBytes();
                                    var string = new String(bytes);
                                    var json = JsonParser.parseString(string);
                                    var value = codec.decode(JsonOps.INSTANCE, json);

                                    var key = resourceEntry.getKey().withPath(s ->
                                            s.replace(".json", "")
                                                    .replace(registryName + "/", ""));

                                    var insertedValue = value.getOrThrow().getFirst();
                                    if(value.isSuccess()) {
                                        Registry.register(
                                                this.registry,
                                                key,
                                                insertedValue
                                        );
                                    } else {
                                        var error = value.error().orElseThrow();
                                        throw new RuntimeException("Error while compiling entry " + resourceEntry.getKey() + " with data " + error.getPartialOrThrow(), new RuntimeException(error.message()));
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            // register our aliases from our remaps
                            var newPath = this.registryName
                                    .replace("engine/", "remap/")
                                    .replace("vars/", "remap/vars/");

                            var mapCodec = Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC);

                            for(var resourceEntry : resourceManager.listResources(newPath, rl -> rl.getPath().endsWith(".json")).entrySet()) {
                                try(var inputStream = resourceEntry.getValue().open()) {
                                    var bytes = inputStream.readAllBytes();
                                    var string = new String(bytes);
                                    var json = JsonParser.parseString(string);
                                    var value = mapCodec.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
                                    this.remapIds.putAll(value);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        },
                        executor
                );
    }
}
