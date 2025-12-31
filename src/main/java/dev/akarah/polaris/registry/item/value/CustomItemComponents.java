package dev.akarah.polaris.registry.item.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.*;

import java.util.Optional;
import java.util.UUID;

public record CustomItemComponents(
        Optional<EquippableData> equippable,
        Identifier placesBlock,
        int maxStackSize,
        Optional<DyedItemColor> color,
        Optional<TrimComponent> trim,
        Optional<ResolvableProfile> profile,
        Optional<BlocksAttacks> blocksAttacks,
        Optional<CustomModelData> customModelData,
        boolean overrideEnchantmentGlint,
        boolean hideTooltip,
        SwingAnimation swingAnimation
) {
    public static Codec<CustomItemComponents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EquippableData.CODEC.optionalFieldOf("equippable").forGetter(CustomItemComponents::equippable),
            Identifier.CODEC
                    .optionalFieldOf("places_block", Identifier.fromNamespaceAndPath("", ""))
                    .forGetter(CustomItemComponents::placesBlock),
            Codec.intRange(1, 99).optionalFieldOf("max_stack_size", 1).forGetter(CustomItemComponents::maxStackSize),
            DyedItemColor.CODEC.optionalFieldOf("color").forGetter(CustomItemComponents::color),
            TrimComponent.CODEC.optionalFieldOf("armor_trim").forGetter(CustomItemComponents::trim),
            ResolvableProfile.CODEC.optionalFieldOf("profile").forGetter(CustomItemComponents::profile),
            BlocksAttacks.CODEC.optionalFieldOf("blocks_attacks").forGetter(CustomItemComponents::blocksAttacks),
            CustomModelData.CODEC.optionalFieldOf("custom_model_data").forGetter(CustomItemComponents::customModelData),
            Codec.BOOL.optionalFieldOf("enchantment_glint_override", false).forGetter(CustomItemComponents::overrideEnchantmentGlint),
            Codec.BOOL.optionalFieldOf("hide_tooltip", false).forGetter(CustomItemComponents::hideTooltip),
            SwingAnimation.CODEC.optionalFieldOf("swing_animation", SwingAnimation.DEFAULT).forGetter(CustomItemComponents::swingAnimation)
    ).apply(instance, CustomItemComponents::new));
}
