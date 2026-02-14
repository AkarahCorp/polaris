package dev.akarah.polaris.registry.item.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

import java.util.Optional;

public record EquippableData(
        EquipmentSlot slot,
        Optional<Identifier> assetId
) {
    public static Codec<EquippableData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EquipmentSlot.CODEC.fieldOf("slot").forGetter(EquippableData::slot),
            Identifier.CODEC.optionalFieldOf("asset_id").forGetter(EquippableData::assetId)
    ).apply(instance, EquippableData::new));

    public Equippable component() {
        var builder = Equippable.builder(slot);
        assetId.ifPresent(id -> builder.setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, id)));
        return builder.build();
    }
}
