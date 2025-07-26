package dev.akarah.cdata.script.type;

import dev.akarah.cdata.registry.stat.StatsObject;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.value.RStatsObject;
import dev.akarah.cdata.script.value.mc.RVector;

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
