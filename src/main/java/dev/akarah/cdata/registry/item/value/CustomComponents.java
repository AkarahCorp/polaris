package dev.akarah.cdata.registry.item.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import javax.swing.text.html.Option;
import java.util.Optional;

public record CustomComponents(
        Optional<EquippableData> equippable,
        ResourceLocation placesBlock,
        int maxStackSize,
        Optional<DyedItemColor> color,
        Optional<TrimComponent> trim
) {
    public static Codec<CustomComponents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EquippableData.CODEC.optionalFieldOf("equippable").forGetter(CustomComponents::equippable),
            ResourceLocation.CODEC
                    .optionalFieldOf("places_block", ResourceLocation.fromNamespaceAndPath("", ""))
                    .forGetter(CustomComponents::placesBlock),
            Codec.intRange(1, 99).optionalFieldOf("max_stack_size", 1).forGetter(CustomComponents::maxStackSize),
            DyedItemColor.CODEC.optionalFieldOf("color").forGetter(CustomComponents::color),
            TrimComponent.CODEC.optionalFieldOf("armor_trim").forGetter(CustomComponents::trim)
    ).apply(instance, CustomComponents::new));
}
