package dev.akarah.polaris.registry.effect;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.stat.StatsObject;
import dev.akarah.polaris.script.value.RNumber;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

public class EffectObject {
    public Holder<CustomEffect> effect;
    public ResourceLocation identifier;
    public int level;
    public int durationRemainingTicks;

    public EffectObject(Holder<CustomEffect> effect, ResourceLocation identifier, int level, int durationRemainingTicks) {
        this.effect = effect;
        this.identifier = identifier;
        this.level = level;
        this.durationRemainingTicks = durationRemainingTicks;
    }

    public StatsObject getStats() {
        var baseStats = effect.value().stats().orElse(CustomEffect.EffectStats.EMPTY).base().orElse(StatsObject.EMPTY);
        var perLevelStats = effect.value().stats().orElse(CustomEffect.EffectStats.EMPTY).perLevel().orElse(StatsObject.EMPTY);
        perLevelStats.multiply(level-1);
        baseStats.add(perLevelStats);
        return baseStats;
    }

    public void tick(){
        if(effect.value().tickingFunction().isEmpty()) {return;}
        Resources.actionManager().executeVoid(effect.value().tickingFunction().get(), RNumber.of((double) level));
    };
}
