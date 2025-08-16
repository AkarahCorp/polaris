package dev.akarah.polaris.registry.item.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.Optional;
import java.util.UUID;

public record CustomComponents(
        Optional<EquippableData> equippable,
        ResourceLocation placesBlock,
        int maxStackSize,
        Optional<DyedItemColor> color,
        Optional<TrimComponent> trim,
        Optional<UUID> playerSkin,
        Optional<BlocksAttacks> blocksAttacks,
        Optional<CustomModelData> customModelData,
        boolean overrideEnchantmentGlint,
        boolean hideTooltip
) {
    public static Codec<CustomComponents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EquippableData.CODEC.optionalFieldOf("equippable").forGetter(CustomComponents::equippable),
            ResourceLocation.CODEC
                    .optionalFieldOf("places_block", ResourceLocation.fromNamespaceAndPath("", ""))
                    .forGetter(CustomComponents::placesBlock),
            Codec.intRange(1, 99).optionalFieldOf("max_stack_size", 1).forGetter(CustomComponents::maxStackSize),
            DyedItemColor.CODEC.optionalFieldOf("color").forGetter(CustomComponents::color),
            TrimComponent.CODEC.optionalFieldOf("armor_trim").forGetter(CustomComponents::trim),
            UUIDUtil.CODEC.optionalFieldOf("player_skin").forGetter(CustomComponents::playerSkin),
            BlocksAttacks.CODEC.optionalFieldOf("blocks_attacks").forGetter(CustomComponents::blocksAttacks),
            CustomModelData.CODEC.optionalFieldOf("custom_model_data").forGetter(CustomComponents::customModelData),
            Codec.BOOL.optionalFieldOf("enchantment_glint_override", false).forGetter(CustomComponents::overrideEnchantmentGlint),
            Codec.BOOL.optionalFieldOf("hide_tooltip", false).forGetter(CustomComponents::hideTooltip)
    ).apply(instance, CustomComponents::new));
}
