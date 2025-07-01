package dev.akarah.cdata.script.env;

import com.google.common.collect.Maps;
import dev.akarah.cdata.registry.text.ParseContext;
import net.minecraft.world.entity.Entity;

import java.util.Map;

public class RuntimeContext {
    Entity primaryEntity;

    public static Map<String, Object> GLOBAL_CONSTANTS = Maps.newHashMap();

    private RuntimeContext(Entity primaryEntity) {
        this.primaryEntity = primaryEntity;
    }

    public static RuntimeContext of(Entity entity) {
        return new RuntimeContext(entity);
    }

    public Entity primaryEntity() {
        return this.primaryEntity;
    }

    public ParseContext parseContext() {
        return ParseContext.empty();
    }
}
