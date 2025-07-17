package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import net.minecraft.network.chat.Component;

public class RText extends RuntimeValue {
    private final Component inner;

    private RText(Component inner) {
        this.inner = inner;
    }

    public static RText of(Component value) {
        return new RText(value);
    }

    @Override
    public Component javaValue() {
        return this.inner;
    }

    @MethodTypeHint("(this: text, color: string) -> text")
    public static RText color(RText $this, RString color) {
        return RText.of($this.javaValue().copy().withColor(Integer.parseInt(color.javaValue(), 16)));
    }
}
