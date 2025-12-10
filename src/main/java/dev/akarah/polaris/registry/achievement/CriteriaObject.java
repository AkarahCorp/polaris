package dev.akarah.polaris.registry.achievement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.Main;
import dev.akarah.polaris.registry.ExtBuiltInRegistries;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.registry.item.CustomItem;
import dev.akarah.polaris.script.value.RBoolean;
import dev.akarah.polaris.script.value.RDict;
import dev.akarah.polaris.script.value.RuntimeValue;
import dev.akarah.polaris.script.value.mc.REntity;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public sealed interface CriteriaObject {
    Identifier name();
    boolean evaluate(ServerPlayer player);

    record AllOf(List<CriteriaObject> criteria) implements CriteriaObject {
        public static Codec<AllOf> CODEC = CriteriaObject.CODEC.listOf().xmap(AllOf::new, AllOf::criteria);

        @Override
        public Identifier name() {
            return Identifier.fromNamespaceAndPath("polaris", "all_of");
        }

        @Override
        public boolean evaluate(ServerPlayer player) {
            for(var obj : criteria) {
                if(!obj.evaluate(player)) {
                    return false;
                }
            }
            return true;
        }
    }

    record AnyOf(List<CriteriaObject> criteria) implements CriteriaObject {
        public static Codec<AnyOf> CODEC = CriteriaObject.CODEC.listOf().xmap(AnyOf::new, AnyOf::criteria);

        @Override
        public Identifier name() {
            return Identifier.fromNamespaceAndPath("polaris", "any_of");
        }

        @Override
        public boolean evaluate(ServerPlayer player) {
            for(var obj : criteria) {
                if(obj.evaluate(player)) {
                    return true;
                }
            }
            return false;
        }
    }

    record HasItem(Identifier id, int min) implements CriteriaObject {
        public static Codec<HasItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(HasItem::id),
                Codec.INT.optionalFieldOf("min", 1).forGetter(HasItem::min)
        ).apply(instance, HasItem::new));

        @Override
        public Identifier name() {
            return Identifier.fromNamespaceAndPath("polaris", "has_item");
        }

        @Override
        public boolean evaluate(ServerPlayer player) {
            for(var item : player.getInventory()) {
                if(CustomItem.itemIdOf(item).orElse(item.getItemHolder().unwrapKey().orElseThrow().identifier()).equals(this.id)) {
                    return true;
                }
            }
            return false;
        }
    }

    record Dynamic(Identifier name, Map<RuntimeValue, RuntimeValue> fields) implements CriteriaObject {
        public static Codec<? extends CriteriaObject> UNNAMED_CODEC =
            Codec
                    .unboundedMap(RuntimeValue.CODEC, RuntimeValue.CODEC)
                    .xmap(map -> new Dynamic(Identifier.fromNamespaceAndPath("", ""), map), Dynamic::fields);

        @Override
        public Identifier name() {
            return this.name;
        }

        @Override
        public boolean evaluate(ServerPlayer player) {
            if(!Resources.ACHIEVEMENT_CRITERIA.registry().containsKey(this.name())) {
                player.sendSystemMessage(Component.literal("Warning: No criteria " + this.name + " found."));
                return false;
            }
            try {
                var criteriaType = Resources.ACHIEVEMENT_CRITERIA.registry().get(this.name()).orElseThrow().value();
                var invocation = (RBoolean) Resources.actionManager().methodHandleByLocation(criteriaType.function()).invoke(REntity.of(player), new RDict(this.fields));
                return invocation.javaValue();
            } catch (Throwable e) {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Codec<CriteriaObject> CODEC =
            Codec.dispatchedMap(
                Identifier.CODEC,
                id -> ExtBuiltInRegistries.CRITERIA_TYPE.get(id).map(Holder.Reference::value).orElse((Codec<CriteriaObject>) Dynamic.UNNAMED_CODEC)
            )
                    .xmap(
                            map -> {
                                var list = (List<CriteriaObject>) new ArrayList<CriteriaObject>();
                                for(var entry : map.entrySet()) {
                                    if(ExtBuiltInRegistries.CRITERIA_TYPE.containsKey(entry.getKey())) {
                                        list.add(entry.getValue());
                                    } else if(entry.getValue() instanceof Dynamic dynamic) {
                                        list.add(new Dynamic(entry.getKey(), dynamic.fields()));
                                    }
                                }
                                return list;
                            },
                            list -> {
                                var map = new HashMap<Identifier, CriteriaObject>();
                                for(var entry : list) {
                                    map.put(entry.name(), entry);
                                }
                                return map;
                            }
                    )
                    .xmap(AllOf::new, AllOf::criteria)
                    .xmap(x -> x, x -> (AllOf) x);

    @SuppressWarnings({"unchecked", "rawtypes"}) // we do some weird type hacks so we tell the compiler to ignore it
    static Object bootStrapCriteriaTypes(Registry<Codec<CriteriaObject>> registry) {
        Registry.register(
                registry,
                Identifier.fromNamespaceAndPath("polaris", "all_of"),
                (Codec) AllOf.CODEC
        );
        Registry.register(
                registry,
                Identifier.fromNamespaceAndPath("polaris", "any_of"),
                (Codec) AnyOf.CODEC
        );
        Registry.register(
                registry,
                Identifier.fromNamespaceAndPath("polaris", "has_item"),
                (Codec) HasItem.CODEC
        );
        Registry.register(
                registry,
                Identifier.fromNamespaceAndPath("polaris", "dynamic"),
                (Codec) Dynamic.UNNAMED_CODEC
        );
        return Dynamic.UNNAMED_CODEC;
    }
}
