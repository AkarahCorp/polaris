package dev.akarah.polaris.registry.achievement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record AchievementCriteriaType(Identifier function) {
    public static Codec<AchievementCriteriaType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("function").forGetter(AchievementCriteriaType::function)
    ).apply(instance, AchievementCriteriaType::new));
}
