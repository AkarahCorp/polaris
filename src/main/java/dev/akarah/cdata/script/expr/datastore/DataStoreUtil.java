package dev.akarah.cdata.script.expr.datastore;

import dev.akarah.cdata.db.DataStore;
import dev.akarah.cdata.db.Database;
import dev.akarah.cdata.script.expr.Expression;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class DataStoreUtil {
    public static DataStore tempDataStoreOf(Entity entity) {
        if(entity instanceof ServerPlayer player) {
            return Database.temp().get("player/" + player.getStringUUID());
        }
        return Database.temp().get("entity/" + entity.getStringUUID());
    }

    public static DataStore saveDataStoreOf(Entity entity) {
        if(entity instanceof ServerPlayer player) {
            return Database.save().get("player/" + player.getStringUUID());
        }
        return Database.save().get("entity/" + entity.getStringUUID());
    }

    public static DataStore tempDataStoreOf(String id) {
        return Database.temp().get(id);
    }

    public static DataStore saveDataStoreOf(String id) {
        return Database.save().get(id);
    }

    public static void bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/save_data"), EntitySaveDataStoreExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/temp_data"), EntityTempDataStoreExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("store/set"), DataStorePutAnyExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("store/get"), DataStoreGetAnyExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("store/set_number"), DataStorePutNumberExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("store/get_number"), DataStoreGetNumberExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("store/temp"), KeyedTempDataStoreExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("store/save"), KeyedSaveDataStoreExpression.class);
    }
}
