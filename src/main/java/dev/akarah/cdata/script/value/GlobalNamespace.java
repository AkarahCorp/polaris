package dev.akarah.cdata.script.value;

import dev.akarah.cdata.Main;
import dev.akarah.cdata.Scheduler;
import dev.akarah.cdata.db.Database;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.registry.item.CustomItem;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.expr.ast.operation.OperationUtil;
import dev.akarah.cdata.script.value.mc.*;
import dev.akarah.cdata.script.value.mc.rt.DynamicContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GlobalNamespace {
    @MethodTypeHint(signature = "(min: number, max: number) -> list[number]", documentation = "Returns a list of numbers from the minimum to the maximum, inclusive.")
    public static RList range(RNumber min, RNumber max) {
        var list = RList.create();

        var newMin = min.javaValue();
        var newMax = max.javaValue();
        var minIsLower = newMin < newMax;

        double idx = minIsLower ? newMin : newMax;
        while(idx <= (minIsLower ? newMax : newMin)) {
            RList.add(list, RNumber.of(idx));
            idx += 1;
        }
        return list;
    }

    @MethodTypeHint(signature = "(x: number, y: number, z: number) -> vector", documentation = "Creates a new vector from X, Y, and Z components.")
    public static RVector vector__create(RNumber x, RNumber y, RNumber z) {
        return RVector.of(new Vec3(x.doubleValue(), y.doubleValue(), z.doubleValue()));
    }

    @MethodTypeHint(signature = "(v: any) -> string", documentation = "Creates a new string from the given value converted to a string.")
    public static RString string__create(RuntimeValue runtimeValue) {
        return RString.of(runtimeValue.toString());
    }

    @MethodTypeHint(signature = "(v: any) -> text", documentation = "Creates a new text from the given value converted to a string.")
    public static RText text__create(RuntimeValue runtimeValue) {
        return RText.of(Component.literal(runtimeValue.toString()).withStyle(s -> s.withItalic(false)));
    }

    public static RText textLiteralInternal(Object runtimeValue) {
        return RText.of(Component.literal(runtimeValue.toString()).withStyle(s -> s.withItalic(false)));
    }

    @MethodTypeHint(signature = "(namespace: string, path: string) -> identifier", documentation = "Returns a new identifier from the namespace and path provided.")
    public static RIdentifier identifier__create(RString namespace, RString path) {
        return RIdentifier.of(ResourceLocation.fromNamespaceAndPath(namespace.javaValue(), path.javaValue()));
    }

    @MethodTypeHint(
            signature = "(item_id: identifier, entity?: entity, application?: function(item) -> void) -> item",
            documentation = "Creates a new custom item, based on the identifier provided. " +
                    "If present, the application function will be invoked on the item after creation before returning the value."
    )
    public static RItem item__create(RIdentifier id, REntity entity, RFunction function) {
        var item = RItem.of(CustomItem.byId(id.javaValue())
                .map(x -> x.toItemStack(RNullable.of(entity)))
                .orElse(ItemStack.EMPTY));
        if(function != null) {
            try {
                function.javaValue().invoke(item, entity);
            } catch (Throwable _) {
                
            }
        }
        return item;
    }

    @MethodTypeHint(
            signature = "(item_id: identifier, template: identifier, entity?: entity, application?: function(item) -> void) -> item",
            documentation = "Creates a new custom item, based on the identifier provided, using the given item template. "
                    + "If present, the application function will be invoked on the item after creation before returning the value."
    )
    public static RItem item__templated(RIdentifier id, RIdentifier template, REntity entity, RFunction function) {
        var item = RItem.of(CustomItem.byId(id.javaValue())
                .map(x -> x.toItemStack(template.javaValue(), RNullable.of(entity)))
                .orElse(ItemStack.EMPTY));
        if(function != null) {
            try {
                function.javaValue().invoke(item, entity);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return item;
    }

    @MethodTypeHint(signature = "(slots: number, items?: list[item], name?: text) -> inventory", documentation = "Creates a new inventory with 27 slots, with the items and name provided.")
    public static RInventory inventory__create(RNumber slots, RList itemList, RText name) {
        var inv = RInventory.of(new DynamicContainer(slots.intValue()), RText.of(Component.literal("Menu")));
        if(itemList != null) {
            for(var item : itemList.javaValue()) {
                if(item instanceof RItem item1) {
                    RInventory.add_item(inv, item1);
                }
            }
        }
        if(name != null) {
            RInventory.set_name(inv, name);
        }
        return inv;
    }

    @MethodTypeHint(signature = "(key: string) -> store", documentation = "Returns the temporary data store with the associated name.")
    public static RStore store__temp(RString key) {
        return RStore.of(Database.temp().get(key.javaValue()));
    }

    @MethodTypeHint(signature = "(key: string) -> store", documentation = "Returns the persistent data store with the associated name.")
    public static RStore store__save(RString key) {
        return RStore.of(Database.save().get(key.javaValue()));
    }

    @MethodTypeHint(signature = "<T>(this: T) -> nullable[T]", documentation = "Returns a nullable instance with the given type.")
    public static RNullable nullable__of(RuntimeValue any) {
        return RNullable.of(any);
    }

    @MethodTypeHint(signature = "() -> nullable[any]", documentation = "Returns an empty nullable instance.")
    public static RNullable nullable__empty() {
        return RNullable.of(null);
    }

    @MethodTypeHint(signature = "(str: string) -> identifier", documentation = "Parses a string into an identifier.")
    public static RIdentifier identifier__parse(RString string) {
        return RIdentifier.of(ResourceLocation.parse(string.javaValue()));
    }

    @MethodTypeHint(signature = "() -> number", documentation = "Returns the number of ticks the server has been up.")
    public static RNumber server__uptime() {
        return RNumber.of(Main.server().getTickCount());
    }

    @MethodTypeHint(signature = "(s: any) -> void", documentation = "Logs a string to the console.")
    public static void debug__log(RuntimeValue value) {
        System.out.println(value.toString());
    }

    @MethodTypeHint(signature = "() -> number", documentation = "Returns a random number between 0 and 1.")
    public static RNumber number__random() {
        return RNumber.of(Math.random());
    }

    @MethodTypeHint(signature = "(delay: number, runnable: function() -> void) -> void", documentation = "Logs a string to the console.")
    public static void run_delayed(RNumber delay, RFunction runnable) {
        Resources.scheduler().schedule(delay.intValue(), () -> {
            try {
                runnable.javaValue().invoke();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }


    @MethodTypeHint(signature = "<T, U>(lhs: T, rhs: U) -> T", documentation = "Adds two values together.")
    public static RuntimeValue add(RuntimeValue lhs, RuntimeValue rhs) {
        return OperationUtil.add(lhs, rhs);
    }
}
