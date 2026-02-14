package dev.akarah.polaris.registry.achievement;

import com.mojang.serialization.Codec;
import net.minecraft.util.Unit;

public record AchievementTrigger() {
    public static Codec<AchievementTrigger> CODEC = Unit.CODEC.xmap(_ -> new AchievementTrigger(), _ -> Unit.INSTANCE);
}
