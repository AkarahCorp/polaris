package dev.akarah.polaris.script.type;

import dev.akarah.polaris.script.jvm.CodegenUtil;
import dev.akarah.polaris.script.value.RStatsObject;

import java.lang.constant.ClassDesc;

public record StatsObjectType() implements Type<RStatsObject> {
    @Override
    public String typeName() {
        return "stat_obj";
    }

    @Override
    public Class<RStatsObject> typeClass() {
        return RStatsObject.class;
    }

    @Override
    public ClassDesc classDescType() {
        return CodegenUtil.ofClass(RStatsObject.class);
    }
}
