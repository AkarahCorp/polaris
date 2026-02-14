package dev.akarah.polaris.script.value.polaris;

import com.mojang.serialization.Lifecycle;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.value.mc.RItemMeta;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class ItemMetaRegistry implements Registry<RItemMeta> {
    @Override
    public ResourceKey<? extends Registry<RItemMeta>> key() {
        throw new NotImplementedException();
    }

    @Override
    public Lifecycle registryLifecycle() {
        throw new NotImplementedException();
    }

    @Override
    public @Nullable Identifier getKey(RItemMeta object) {
        return Resources.customItem().registry().getKey(object.javaValue());
    }

    @Override
    public Optional<ResourceKey<RItemMeta>> getResourceKey(RItemMeta object) {
        throw new NotImplementedException();
    }

    @Override
    public int getId(@Nullable RItemMeta object) {
        throw new NotImplementedException();
    }

    @Override
    public @Nullable RItemMeta byId(int i) {
        throw new NotImplementedException();
    }

    @Override
    public int size() {
        return Resources.customItem().registry().size();
    }

    @Override
    public @Nullable RItemMeta getValue(@Nullable ResourceKey<RItemMeta> resourceKey) {
        throw new NotImplementedException();
    }

    @Override
    public @Nullable RItemMeta getValue(@Nullable Identifier identifier) {
        if(!Resources.customItem().registry().containsKey(identifier)) {
            return null;
        }
        return RItemMeta.of(Resources.customItem().registry().getValue(identifier));
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<RItemMeta> resourceKey) {
        throw new NotImplementedException();
    }

    @Override
    public Optional<Holder.Reference<RItemMeta>> getAny() {
        throw new NotImplementedException();
    }

    @Override
    public Set<Identifier> keySet() {
        return Resources.customItem().registry().keySet();
    }

    @Override
    public Set<Map.Entry<ResourceKey<RItemMeta>, RItemMeta>> entrySet() {
        throw new NotImplementedException();
    }

    @Override
    public Set<ResourceKey<RItemMeta>> registryKeySet() {
        throw new NotImplementedException();
    }

    @Override
    public Optional<Holder.Reference<RItemMeta>> getRandom(RandomSource randomSource) {
        throw new NotImplementedException();
    }

    @Override
    public boolean containsKey(Identifier identifier) {
        throw new NotImplementedException();
    }

    @Override
    public boolean containsKey(ResourceKey<RItemMeta> resourceKey) {
        throw new NotImplementedException();
    }

    @Override
    public Registry<RItemMeta> freeze() {
        throw new NotImplementedException();
    }

    @Override
    public Holder.Reference<RItemMeta> createIntrusiveHolder(RItemMeta object) {
        throw new NotImplementedException();
    }

    @Override
    public Optional<Holder.Reference<RItemMeta>> get(int i) {
        throw new NotImplementedException();
    }

    @Override
    public Optional<Holder.Reference<RItemMeta>> get(Identifier identifier) {
        throw new NotImplementedException();
    }

    @Override
    public Holder<RItemMeta> wrapAsHolder(RItemMeta object) {
        throw new NotImplementedException();
    }

    @Override
    public Stream<HolderSet.Named<RItemMeta>> getTags() {
        throw new NotImplementedException();
    }

    @Override
    public PendingTags<RItemMeta> prepareTagReload(TagLoader.LoadResult<RItemMeta> loadResult) {
        throw new NotImplementedException();
    }

    @Override
    public @NonNull Iterator<RItemMeta> iterator() {
        throw new NotImplementedException();
    }

    @Override
    public Stream<Holder.Reference<RItemMeta>> listElements() {
        throw new NotImplementedException();
    }

    @Override
    public Stream<HolderSet.Named<RItemMeta>> listTags() {
        throw new NotImplementedException();
    }

    @Override
    public Optional<Holder.Reference<RItemMeta>> get(ResourceKey<RItemMeta> resourceKey) {
        throw new NotImplementedException();
    }

    @Override
    public Optional<HolderSet.Named<RItemMeta>> get(TagKey<RItemMeta> tagKey) {
        throw new NotImplementedException();
    }
}
