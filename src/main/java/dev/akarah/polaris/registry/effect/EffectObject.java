package dev.akarah.polaris.registry.effect;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.stat.StatsObject;
import dev.akarah.polaris.script.value.RNumber;
import dev.akarah.polaris.script.value.mc.REntity;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class EffectObject {
    public Holder<CustomEffect> effect;
    public int level;
    public int durationRemainingTicks;

    public EffectObject(Holder<CustomEffect> effect, int level, int durationRemainingTicks) {
        this.effect = effect;
        this.level = Math.min(level, effect.value().maxLevel());
        this.durationRemainingTicks = durationRemainingTicks;
    }

    public StatsObject getStats() {
        var baseStats = effect.value().stats().orElse(CustomEffect.EffectStats.EMPTY).base().orElse(StatsObject.EMPTY);
        var perLevelStats = effect.value().stats().orElse(CustomEffect.EffectStats.EMPTY).perLevel().orElse(StatsObject.EMPTY);
        perLevelStats.multiply(level-1);
        baseStats.addAllUnderName(perLevelStats, Component.literal(""));
        return baseStats;
    }

    public void tick(Player player){
        durationRemainingTicks -= 1;
        if(durationRemainingTicks <= 0) {return;}
        if(effect.value().tickingFunction().isEmpty()) {return;}
        Resources.actionManager().executeVoid(effect.value().tickingFunction().get(), RNumber.of(level), REntity.of(player));
    };
}
