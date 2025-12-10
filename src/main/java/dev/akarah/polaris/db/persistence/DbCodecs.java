package dev.akarah.polaris.db.persistence;

import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.item.CustomItem;
import dev.akarah.polaris.script.value.*;
import dev.akarah.polaris.script.value.mc.RIdentifier;
import dev.akarah.polaris.script.value.mc.RItem;
import dev.akarah.polaris.script.value.mc.RVector;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DbCodecs {
    public static StreamCodec<RegistryFriendlyByteBuf, RuntimeValue> DYNAMIC_CODEC = StreamCodec.recursive(selfCodec -> StreamCodec.of(
            (buf, object) -> {
                switch (object) {
                    case RNumber d -> {
                        buf.writeVarInt(1);
                        buf.writeDouble(d.doubleValue());
                    }
                    case RString s -> {
                        buf.writeVarInt(2);
                        buf.writeByteArray(s.javaValue().getBytes(StandardCharsets.UTF_8));
                    }
                    case RList arrayList -> {
                        buf.writeVarInt(3);
                        buf.writeVarInt(arrayList.javaValue().size());
                        for(var element : arrayList.javaValue()) {
                            selfCodec.encode(buf, element);
                        }
                    }
                    case RDict map -> {
                        buf.writeVarInt(4);
                        buf.writeVarInt(map.javaValue().size());
                        for(var entry : map.javaValue().entrySet()) {
                            selfCodec.encode(buf, entry.getKey());
                            selfCodec.encode(buf, entry.getValue());
                        }
                    }
                    case RStruct struct -> {
                        buf.writeVarInt(5);
                        buf.writeVarInt(struct.javaValue().size());
                        buf.writeUtf(struct.name());
                        for(var entry : struct.javaValue().entrySet()) {
                            buf.writeUtf(entry.getKey());
                            selfCodec.encode(buf, entry.getValue());
                        }
                    }
                    case RVector vector -> {
                        buf.writeVarInt(6);
                        Vec3.STREAM_CODEC.encode(buf, vector.javaValue());
                    }
                    case RUuid uuid -> {
                        buf.writeVarInt(7);
                        UUIDUtil.STREAM_CODEC.encode(buf, uuid.javaValue());
                    }
                    case RItem item -> CustomItem.itemOf(item.javaValue()).ifPresentOrElse(
                            customItem -> {
                                var cd = item.javaValue().get(DataComponents.CUSTOM_DATA);
                                if(cd == null) {
                                    buf.writeVarInt(8);
                                } else {
                                    buf.writeVarInt(13);
                                    ByteBufCodecs.COMPOUND_TAG.encode(buf, cd.copyTag());
                                }
                                buf.writeIdentifier(customItem.id());
                                buf.writeVarInt(item.javaValue().getCount());
                            },
                            () -> buf.writeVarInt(9)
                    );
                    case RIdentifier identifier -> {
                        buf.writeVarInt(10);
                        buf.writeIdentifier(identifier.javaValue());
                    }
                    case RBoolean rBoolean -> {
                        if(rBoolean.javaValue()) {
                            buf.writeVarInt(11);
                        } else {
                            buf.writeVarInt(12);
                        }
                    }
                    case RTimestamp timestamp -> {
                        buf.writeVarInt(14);
                        buf.writeVarLong(timestamp.javaValue().getEpochSecond());
                        buf.writeVarLong(timestamp.javaValue().getNano());
                    }
                    default -> throw new RuntimeException("Unable to make a codec out of " + object);
                }
            },
            (buf) -> {
                var id = buf.readVarInt();
                switch (id) {
                    case 1 -> {
                        return RNumber.of(buf.readDouble());
                    }
                    case 2 -> {
                        return RString.of(new String(buf.readByteArray(), StandardCharsets.UTF_8));
                    }
                    case 3 -> {
                        var list = RList.create();
                        var size = buf.readVarInt();
                        for(int i = 0; i < size; i++) {
                            RList.add(list, selfCodec.decode(buf));
                        }
                        return list;
                    }
                    case 4 -> {
                        var map = RDict.create();
                        var size = buf.readVarInt();
                        for(int i = 0; i < size; i++) {
                            RDict.put(map, selfCodec.decode(buf), selfCodec.decode(buf));
                        }
                        return map;
                    }
                    case 5 -> {
                        var size = buf.readVarInt();
                        var name = buf.readUtf();
                        var struct = RStruct.create(name, size);
                        for(int i = 0; i < size; i++) {
                            var field = buf.readUtf();
                            RStruct.put(struct, field, selfCodec.decode(buf));
                        }
                        return struct;
                    }
                    case 6 -> {
                        return RVector.of(Vec3.STREAM_CODEC.decode(buf));
                    }
                    case 7 -> {
                        return RUuid.of(UUIDUtil.STREAM_CODEC.decode(buf));
                    }
                    case 8 -> {
                        var itemId = buf.readIdentifier();
                        var itemSize = buf.readVarInt();
                        return RItem.of(Resources.customItem().registry().get(itemId)
                                .map(x -> x.value().toMinimalItemStack(CustomData.EMPTY, itemSize))
                                .orElse(ItemStack.EMPTY));
                    }
                    case 9 -> {
                        return RItem.of(ItemStack.EMPTY);
                    }
                    case 10 -> {
                        return RIdentifier.of(buf.readIdentifier());
                    }
                    case 11 -> {
                        return RBoolean.of(true);
                    }
                    case 12 -> {
                        return RBoolean.of(false);
                    }
                    case 13 -> {
                        var customData = ByteBufCodecs.COMPOUND_TAG.decode(buf);
                        var itemId = buf.readIdentifier();
                        var itemSize = buf.readVarInt();
                        return RItem.of(Resources.customItem().registry().get(itemId)
                                .map(x -> x.value().toMinimalItemStack(CustomData.of(customData), 1).copyWithCount(itemSize))
                                .orElse(ItemStack.EMPTY));
                    }
                    case 14 -> {
                        var seconds = buf.readVarLong();
                        var nanos = buf.readVarLong();
                        return RTimestamp.of(Instant.ofEpochSecond(seconds, nanos));
                    }
                    default -> throw new RuntimeException("Unknown id " + id);
                }
            }
    ));

    public static StreamCodec<RegistryFriendlyByteBuf, Object2ObjectAVLTreeMap<String, RuntimeValue>> TREE_MAP_CODEC = StreamCodec.of(
            (buf, map) -> {
                buf.writeVarInt(map.size());
                for(var entry : map.entrySet()) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, entry.getKey());
                    DYNAMIC_CODEC.encode(buf, entry.getValue());
                }
            },
            (buf) -> {
                var map = new Object2ObjectAVLTreeMap<String, RuntimeValue>();
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
