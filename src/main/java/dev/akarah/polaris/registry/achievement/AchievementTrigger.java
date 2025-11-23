package dev.akarah.polaris.registry.achievement;

import com.mojang.serialization.Codec;

public record AchievementTrigger() {
    public static Codec<AchievementTrigger> CODEC = Codec.unit(AchievementTrigger::new);
}
