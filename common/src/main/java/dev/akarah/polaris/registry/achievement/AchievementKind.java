package dev.akarah.polaris.registry.achievement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.script.value.RNullable;
import dev.akarah.polaris.script.value.RuntimeValue;
import net.minecraft.resources.Identifier;

import java.util.List;

public record AchievementKind(
        String name,
        String description,
        RuntimeValue customData,
        List<Identifier> triggers,
        CriteriaObject criteria,
        Identifier parent,
        Identifier icon
) {
    public static Codec<AchievementKind> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(AchievementKind::name),
            Codec.STRING.fieldOf("description").forGetter(AchievementKind::description),
            RuntimeValue.CODEC.optionalFieldOf("custom_data", RNullable.empty()).forGetter(AchievementKind::customData),
            Identifier.CODEC.listOf().fieldOf("triggers").forGetter(AchievementKind::triggers),
            CriteriaObject.CODEC.optionalFieldOf("criteria", new CriteriaObject.AllOf(List.of())).forGetter(AchievementKind::criteria),
            Identifier.CODEC.optionalFieldOf("parent", Identifier.fromNamespaceAndPath("akarahnet", "root")).forGetter(AchievementKind::parent),
            Identifier.CODEC.optionalFieldOf("icon", Identifier.fromNamespaceAndPath("minecraft", "barrier")).forGetter(AchievementKind::icon)
    ).apply(instance, AchievementKind::new));
}
