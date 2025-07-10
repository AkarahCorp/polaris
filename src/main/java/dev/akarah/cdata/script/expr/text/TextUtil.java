package dev.akarah.cdata.script.expr.text;

import dev.akarah.cdata.script.expr.Expression;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class TextUtil {
    public static void bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("text"), ComponentLiteralFuncExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("text/color"), ComponentColorExpression.class);
    }

    public static MutableComponent loreFriendlyLiteral(String value) {
        return Component.literal(value)
                .withStyle(Style.EMPTY.withItalic(false))
                .withColor(0xFFFFFF);
    }

    public static MutableComponent withColor(MutableComponent component, String color) {
        return component.withColor(Integer.parseInt(color, 16));
    }
}
