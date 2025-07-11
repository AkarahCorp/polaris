package dev.akarah.cdata.db.persistence;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbCodecs {
    public static StreamCodec<RegistryFriendlyByteBuf, Object> DYNAMIC_CODEC = StreamCodec.recursive(selfCodec -> StreamCodec.of(
            (buf, object) -> {
                switch (object) {
                    case Double d -> {
                        buf.writeVarInt(1);
                        buf.writeDouble(d);
                    }
                    case String s -> {
                        buf.writeVarInt(2);
                        buf.writeByteArray(s.getBytes(StandardCharsets.UTF_8));
                    }
                    case List<?> arrayList -> {
                        buf.writeVarInt(3);
                        buf.writeVarInt(arrayList.size());
                        for(var element : arrayList) {
                            selfCodec.encode(buf, element);
                        }
                    }
                    case Map<?, ?> map -> {
                        buf.writeVarInt(4);
                        buf.writeVarInt(map.size());
                        for(var entry : map.entrySet()) {
                            selfCodec.encode(buf, entry.getKey());
                            selfCodec.encode(buf, entry.getValue());
                        }
                    }
                    default -> throw new RuntimeException("Unable to make a codec out of " + object);
                }
            },
            (buf) -> {
                var id = buf.readVarInt();
                switch (id) {
                    case 1 -> {
                        return buf.readDouble();
                    }
                    case 2 -> {
                        return new String(buf.readByteArray(), StandardCharsets.UTF_8);
                    }
                    case 3 -> {
                        var list = new ArrayList<>();
                        var size = buf.readVarInt();
                        for(int i = 0; i < size; i++) {
                            list.add(selfCodec.decode(buf));
                        }
                        return list;
                    }
                    case 4 -> {
                        var map = new HashMap<>();
                        var size = buf.readVarInt();
                        for(int i = 0; i < size; i++) {
                            map.put(selfCodec.decode(buf), selfCodec.decode(buf));
                        }
                        return map;
                    }
                    default -> throw new RuntimeException("Unknown id " + id);
                }
            }
    ));

    public static StreamCodec<RegistryFriendlyByteBuf, Object2ObjectAVLTreeMap<String, Object>> TREE_MAP_CODEC = StreamCodec.of(
            (buf, map) -> {
                buf.writeVarInt(map.size());
                for(var entry : map.entrySet()) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, entry.getKey());
                    DYNAMIC_CODEC.encode(buf, entry.getValue());
                }
            },
            (buf) -> {
                var map = new Object2ObjectAVLTreeMap<String, Object>();
                var entries = buf.readVarInt();
                for(int i = 0; i < entries; i++) {
                    var key = ByteBufCodecs.STRING_UTF8.decode(buf);
                    var value = DYNAMIC_CODEC.decode(buf);
                    map.put(key, value);
                }
                return map;
            }
    );


}
