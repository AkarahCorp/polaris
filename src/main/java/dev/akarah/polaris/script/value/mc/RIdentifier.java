package dev.akarah.polaris.script.value.mc;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.value.RString;
import dev.akarah.polaris.script.value.RuntimeValue;
import net.minecraft.resources.ResourceLocation;

public class RIdentifier extends RuntimeValue {
    private final ResourceLocation inner;

    private RIdentifier(ResourceLocation inner) {
        this.inner = inner;
    }

    public static RIdentifier of(ResourceLocation value) {
        return new RIdentifier(value);
    }

    public static RIdentifier of(String namespace, String path) {
        return new RIdentifier(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    @MethodTypeHint(signature = "(this: identifier, append: string) -> identifier", documentation = "Returns a new resource location, with the string appended to the end.")
    public static RIdentifier add(RIdentifier $this, RString append) {
        return RIdentifier.of(ResourceLocation.parse($this.toString() + append));
    }

    @Override
    public ResourceLocation javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return RIdentifier.of(this.inner);
    }
}
