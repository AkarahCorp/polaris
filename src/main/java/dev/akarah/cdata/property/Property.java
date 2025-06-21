package dev.akarah.cdata.property;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.ExtRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public sealed interface Property<T> permits Property.Impl {
    Codec<T> codec();
    void applyToItem(ItemStack item, T value);

    Codec<Property<?>> CODEC = Codec.lazyInitialized(() -> ExtBuiltInRegistries.PROPERTIES.byNameCodec());
    Codec<Map<Property<?>, Object>> VALUE_MAP_CODEC = Codec.dispatchedMap(CODEC, Property::codec);

    static <T> Builder<T> builder() {
        return new Builder<>();
    }

    class Builder<T> {
        private static final BiConsumer<ItemStack, Object> ITEM_APP_NOOP = (item, value) -> {};

        private Codec<T> codec;

        @SuppressWarnings("unchecked")
        private BiConsumer<ItemStack, T> itemApplication = (BiConsumer<ItemStack, T>) ITEM_APP_NOOP;

        private ResourceLocation id;

        public Builder<T> codec(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        public Builder<T> itemApplication(BiConsumer<ItemStack, T> consumer) {
            this.itemApplication = consumer;
            return this;
        }

        public Property<T> build() {
            Preconditions.checkArgument(this.codec != null, "Codec supplied to Property.Builder must be non-null");
            return new Impl<>(this.codec, this.itemApplication);
        }
    }

    record Impl<T>(Codec<T> codec, BiConsumer<ItemStack, T> itemApplication) implements Property<T> {
        @Override
        public void applyToItem(ItemStack item, T value) {
            itemApplication.accept(item, value);
        }

        @Override
        public @NotNull String toString() {
            return Objects.requireNonNull(ExtBuiltInRegistries.PROPERTIES.getKey(this))
                    .toString();
        }
    }
}
