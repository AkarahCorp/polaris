package dev.akarah.polaris.registry.achievement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.script.value.RNullable;
import dev.akarah.polaris.script.value.RuntimeValue;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record AchievementKind(
        String name,
        String description,
        RuntimeValue customData,
        List<ResourceLocation> triggers,
        CriteriaObject criteria,
        ResourceLocation parent,
        ResourceLocation icon
) {
    public static Codec<AchievementKind> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(AchievementKind::name),
            Codec.STRING.fieldOf("description").forGetter(AchievementKind::description),
            RuntimeValue.CODEC.optionalFieldOf("custom_data", RNullable.empty()).forGetter(AchievementKind::customData),
            ResourceLocation.CODEC.listOf().fieldOf("triggers").forGetter(AchievementKind::triggers),
            CriteriaObject.CODEC.optionalFieldOf("criteria", new CriteriaObject.AllOf(List.of())).forGetter(AchievementKind::criteria),
            ResourceLocation.CODEC.optionalFieldOf("parent", ResourceLocation.fromNamespaceAndPath("akarahnet", "root")).forGetter(AchievementKind::parent),
            ResourceLocation.CODEC.optionalFieldOf("icon", ResourceLocation.fromNamespaceAndPath("minecraft", "barrier")).forGetter(AchievementKind::icon)
    ).apply(instance, AchievementKind::new));
}
