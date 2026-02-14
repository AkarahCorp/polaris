package dev.akarah.polaris.script.value.mc;

import dev.akarah.polaris.registry.item.CustomItem;
import dev.akarah.polaris.script.value.RuntimeValue;

public class RItemMeta extends RuntimeValue {
    private final CustomItem inner;

    private RItemMeta(CustomItem inner) {
        this.inner = inner;
    }

    public static RItemMeta of(CustomItem value) {
        return new RItemMeta(value);
    }


    public static String typeName() {
        return "item_meta";
    }

    @Override
    public CustomItem javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return RItemMeta.of(this.inner);
    }
}
