package dev.akarah.cdata.registry.entity;

import dev.akarah.cdata.script.env.ScriptContext;
import dev.akarah.cdata.script.env.Selection;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DynamicEntity extends LivingEntity {
    CustomEntity base;

    public DynamicEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level level,
            CustomEntity base
    ) {
        super(entityType, level);
        this.base = base;
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        var ctx = ScriptContext.of(Selection.of(this));
        this.base.onTick().ifPresent(x -> x.execute(ctx));
    }
}
