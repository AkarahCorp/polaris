package dev.akarah.polaris.script.value.mc;

import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.value.RString;
import dev.akarah.polaris.script.value.RuntimeValue;
import net.minecraft.resources.Identifier;

public class RIdentifier extends RuntimeValue {
    private final Identifier inner;


    public static String typeName() {
        return "identifier";
    }

    private RIdentifier(Identifier inner) {
        this.inner = inner;
    }

    public static RIdentifier of(Identifier value) {
        return new RIdentifier(value);
    }

    public static RIdentifier of(String namespace, String path) {
        return new RIdentifier(Identifier.fromNamespaceAndPath(namespace, path));
    }

    @MethodTypeHint(signature = "(this: identifier, append: string) -> identifier", documentation = "Returns a new resource location, with the string appended to the end.")
    public static RIdentifier add(RIdentifier $this, RString append) {
        return RIdentifier.of(Identifier.parse($this.toString() + append));
    }

    @Override
    public Identifier javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        return RIdentifier.of(this.inner);
    }
}
