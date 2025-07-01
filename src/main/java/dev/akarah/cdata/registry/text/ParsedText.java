package dev.akarah.cdata.registry.text;

import com.mojang.serialization.Codec;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.text.arguments.FunctionCall;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public record ParsedText(
        String string,
        List<FunctionCall> interpolations
) {
    public static Codec<ParsedText> CODEC = Codec.STRING.xmap(
            Parser::parseTextLine,
            x -> { throw new RuntimeException("ummm no sorry"); }
    );

    public Optional<Component> output(ParseContext environment) {
        var mm = MiniMessage.miniMessage();
        var component = mm.deserialize(this.string, tagResolver(environment));

        var plainText = PlainTextComponentSerializer.plainText().serialize(component);
        // interpolation failed
        // this also works for %skip, since throwing an exception gets caught in the tag resolver
        if(plainText.contains("<interpolate:")) {
            return Optional.empty();
        }
        return Optional.of(Main.AUDIENCES.asNative(component));
    }

    public Component outputOrNull(ParseContext environment) {
        return this.output(environment).orElse(null);
    }

    public TagResolver tagResolver(ParseContext environment) {
        return TagResolver.resolver(
                "interpolate",
                (args, ctx) -> {
                    var value = this.interpolations.get(args.pop().asInt().orElse(0))
                            .evaluate(environment);
                    while(value instanceof FunctionCall call) {
                        value = call.evaluate(environment);
                    }
                    return Tag.inserting(ctx.deserialize(value.toString()));
                }
        );
    }
}
