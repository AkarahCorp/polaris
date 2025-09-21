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

public record CustomItemComponents(
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
    public static Codec<CustomItemComponents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EquippableData.CODEC.optionalFieldOf("equippable").forGetter(CustomItemComponents::equippable),
            ResourceLocation.CODEC
                    .optionalFieldOf("places_block", ResourceLocation.fromNamespaceAndPath("", ""))
                    .forGetter(CustomItemComponents::placesBlock),
            Codec.intRange(1, 99).optionalFieldOf("max_stack_size", 1).forGetter(CustomItemComponents::maxStackSize),
            DyedItemColor.CODEC.optionalFieldOf("color").forGetter(CustomItemComponents::color),
            TrimComponent.CODEC.optionalFieldOf("armor_trim").forGetter(CustomItemComponents::trim),
            UUIDUtil.CODEC.optionalFieldOf("player_skin").forGetter(CustomItemComponents::playerSkin),
            BlocksAttacks.CODEC.optionalFieldOf("blocks_attacks").forGetter(CustomItemComponents::blocksAttacks),
            CustomModelData.CODEC.optionalFieldOf("custom_model_data").forGetter(CustomItemComponents::customModelData),
            Codec.BOOL.optionalFieldOf("enchantment_glint_override", false).forGetter(CustomItemComponents::overrideEnchantmentGlint),
            Codec.BOOL.optionalFieldOf("hide_tooltip", false).forGetter(CustomItemComponents::hideTooltip)
    ).apply(instance, CustomItemComponents::new));
}
