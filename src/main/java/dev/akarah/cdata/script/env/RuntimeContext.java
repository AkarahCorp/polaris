package dev.akarah.cdata.script.env;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.Entity;

import java.util.Map;

public class RuntimeContext {
    Entity primaryEntity;
    Map<String, Object> localVariables;

    private RuntimeContext(Entity primaryEntity, Map<String, Object> localVariables) {
        this.primaryEntity = primaryEntity;
        this.localVariables = localVariables;
    }

    public static RuntimeContext of(Entity entity) {
        return new RuntimeContext(entity, Maps.newHashMap());
    }
}
