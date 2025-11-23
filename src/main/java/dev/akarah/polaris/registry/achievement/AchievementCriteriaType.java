package dev.akarah.polaris.registry.achievement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record AchievementCriteriaType(ResourceLocation function) {
    public static Codec<AchievementCriteriaType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("function").forGetter(AchievementCriteriaType::function)
    ).apply(instance, AchievementCriteriaType::new));
}
