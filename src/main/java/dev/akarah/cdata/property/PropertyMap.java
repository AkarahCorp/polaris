package dev.akarah.cdata.property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface PropertyMap {
    <T> Optional<T> get(Property<T> property);
    Set<Property<?>> keySet();

    default int size() {
        return this.keySet().size();
    }

    public static PropertyMap EMPTY = new PropertyMap() {
        @Override
        public <T> Optional<T> get(Property<T> property) {
            return Optional.empty();
        }

        @Override
        public Set<Property<?>> keySet() {
            return Set.of();
        }

        @Override
        public @NotNull String toString() {
            return "{}";
        }
    };

    @SuppressWarnings("unchecked")
    Codec<PropertyMap> CODEC = Codec.dispatchedMap(Property.CODEC, x -> (Codec<Object>) x.codec())
            .flatComapMap(Builder::buildFromMap, propertyMap -> {
                var size = propertyMap.size();
                if(size == 0) {
                    return DataResult.success(Map.of());
                }

                var map = new Reference2ObjectOpenHashMap<Property<?>, Object>();
                map.replaceAll((k, v) -> propertyMap.get((Property<?>) k));
                return DataResult.success(map);
            });

    default <T> boolean has(Property<T> property) {
        return this.get(property).isPresent();
    }

    static Builder builder() {
        return new Builder(new Reference2ObjectOpenHashMap<>());
    }

    class Builder {
        Reference2ObjectMap<Property<?>, Object> map;

        private Builder(Reference2ObjectMap<Property<?>, Object> map) {
            this.map = map;
        }

        public <T> Builder set(Property<T> property, T value) {
            this.map.put(property, value);
            return this;
        }

        private void setUnchecked(Property<?> property, Object value) {
            this.map.put(property, value);
        }

        public PropertyMap build() {
            return new SimpleMap(this.map);
        }

        public static PropertyMap buildFromMap(Map<Property<?>, Object> map) {
            if(map.isEmpty()) {
                return PropertyMap.EMPTY;
            }

            var b = PropertyMap.builder();
            for(var key : map.keySet()) {
                b.setUnchecked(key, map.get(key));
            }
            return b.build();
        }
    }

    record SimpleMap(Reference2ObjectMap<Property<?>, Object> map) implements PropertyMap {
        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> get(Property<T> property) {
            return Optional.ofNullable((T) this.map.get(property));
        }

        @Override
        public Set<Property<?>> keySet() {
            return this.map.keySet();
        }

        @Override
        public @NotNull String toString() {
            return this.map.toString();
        }
    }
}
