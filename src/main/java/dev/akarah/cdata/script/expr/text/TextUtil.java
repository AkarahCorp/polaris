package dev.akarah.cdata.script.expr.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TextUtil {
    public static MutableComponent loreFriendlyLiteral(String value) {
        return Component.literal(value)
                .withStyle(Style.EMPTY.withItalic(false))
                .withColor(0xFFFFFF);
    }
}
