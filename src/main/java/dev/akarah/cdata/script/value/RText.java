package dev.akarah.cdata.script.value;

import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;
import dev.akarah.cdata.script.value.mc.RItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

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

    @MethodTypeHint(signature = "(this: text, color: string) -> text", documentation = "Changes the color of the text to the hex code provided.")
    public static RText color(RText $this, RString color) {
        return RText.of($this.javaValue().copy().withColor(Integer.parseInt(color.javaValue(), 16)));
    }

    @MethodTypeHint(signature = "(this: text, hover_item: item) -> text", documentation = "Shows the item as a tooltip on hover.")
    public static RText hover_item(RText $this, RItem item) {
        return RText.of($this.javaValue().copy().withStyle(style -> style.withHoverEvent(
                new HoverEvent.ShowItem(item.javaValue())
        )));
    }

    @MethodTypeHint(signature = "(this: text) -> string", documentation = "Returns the action content of the text component.")
    public static RString contents(RText $this) {
        return RString.of($this.javaValue().copy().getString());
    }

    @MethodTypeHint(signature = "(base: text, concat: any) -> text", documentation = "Returns a new string, concatenating 2 strings.")
    public static RText add(RText base, RuntimeValue convert) {
        if(convert instanceof RText text) {
            return RText.of(base.javaValue().copy().append(text.javaValue()));
        }
        return RText.of(base.javaValue().copy().append(convert.toString()));
    }
}
