package dev.akarah.polaris.registry;

import com.google.common.collect.Sets;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public record RegistryWrapper<T>(
        Registry<T> root,
        Map<Identifier, Identifier> remaps
) implements Registry<T> {
    @Override
    public ResourceKey<? extends Registry<T>> key() {
        return root.key();
    }

    @Override
    public Lifecycle registryLifecycle() {
        return root.registryLifecycle();
    }

    @Override
    public @Nullable Identifier getKey(T object) {
        return root.getKey(object);
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T object) {
        return root.getResourceKey(object);
    }

    @Override
    public int getId(@Nullable T object) {
        return root.getId(object);
    }

    @Override
    public @Nullable T byId(int i) {
        return root.byId(i);
    }

    @Override
    public int size() {
        return root.size();
    }

    @Override
    public @Nullable T getValue(@Nullable ResourceKey<T> resourceKey) {
        if(resourceKey == null) {
            return null;
        }
        return this.getValue(resourceKey.identifier());
    }

    @Override
    public @Nullable T getValue(@Nullable Identifier identifier) {
        if(identifier == null) {
            return null;
        }
        if(this.remaps.containsKey(identifier)) {
            return root.getValue(this.remaps.get(identifier));
        }
        return root.getValue(identifier);
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> resourceKey) {
        return root.registrationInfo(resourceKey);
    }

    @Override
    public Optional<Holder.Reference<T>> getAny() {
        return root.getAny();
    }

    @Override
    public Set<Identifier> keySet() {
        return Sets.union(root.keySet(), this.remaps.keySet());
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return root.entrySet();
    }

    @Override
    public @NonNull Set<ResourceKey<T>> registryKeySet() {
        return root.registryKeySet();
    }

    @Override
    public @NonNull Optional<Holder.Reference<T>> getRandom(@NonNull RandomSource randomSource) {
        return root.getRandom(randomSource);
    }

    @Override
    public boolean containsKey(@NonNull Identifier identifier) {
        return root.containsKey(identifier) || this.remaps.containsKey(identifier);
    }

    @Override
    public boolean containsKey(@NonNull ResourceKey<T> resourceKey) {
        return root.containsKey(resourceKey) || this.remaps.containsKey(resourceKey.identifier());
    }

    @Override
    public @NonNull Registry<T> freeze() {
        return root.freeze();
    }

    @Override
    public Holder.@NonNull Reference<T> createIntrusiveHolder(T object) {
        return root.createIntrusiveHolder(object);
    }

    @Override
    public @NonNull Optional<Holder.Reference<T>> get(int i) {
        return root.get(i);
    }

    @Override
    public @NonNull Optional<Holder.Reference<T>> get(Identifier identifier) {
        if(this.remaps.containsKey(identifier)) {
            return this.get(this.remaps.get(identifier));
        }
        return root.get(identifier);
    }

    @Override
    public @NonNull Holder<T> wrapAsHolder(T object) {
        return root.wrapAsHolder(object);
    }

    @Override
    public @NonNull Stream<HolderSet.Named<T>> getTags() {
        return root.getTags();
    }

    @Override
    public @NonNull PendingTags<T> prepareTagReload(TagLoader.@NonNull LoadResult<T> loadResult) {
        return root.prepareTagReload(loadResult);
    }

    @Override
    public @NonNull Iterator<T> iterator() {
        return root.iterator();
    }

    @Override
    public @NonNull Stream<Holder.Reference<T>> listElements() {
        return root.listElements();
    }

    @Override
    public @NonNull Stream<HolderSet.Named<T>> listTags() {
        return root.listTags();
    }

    @Override
    public @NonNull Optional<Holder.Reference<T>> get(@NonNull ResourceKey<T> resourceKey) {
        if(this.remaps.containsKey(resourceKey.identifier())) {
            return this.get(this.remaps.get(resourceKey.identifier()));
        }
        return root.get(resourceKey);
    }

    @Override
    public @NonNull Optional<HolderSet.Named<T>> get(@NonNull TagKey<T> tagKey) {
        return root.get(tagKey);
    }
}
