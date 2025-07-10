package dev.akarah.cdata.db;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;

import java.util.Map;

public class Database {
    Map<String, DataStore> dataStores = new Object2ObjectAVLTreeMap<>();

    private static Database LOCAL;
    private static Database GLOBAL;

    private Database() {

    }

    public static Database temp() {
        if(LOCAL == null) {
            LOCAL = new Database();
        }
        return LOCAL;
    }

    public static Database save() {
        if(GLOBAL == null) {
            GLOBAL = new Database();
        }
        return GLOBAL;
    }

    public DataStore get(String key) {
        var value = dataStores.get(key);
        if(value == null) {
            var ds = DataStore.of();
            dataStores.put(key, ds);
            return ds;
        } else {
            return value;
        }
    }
}
