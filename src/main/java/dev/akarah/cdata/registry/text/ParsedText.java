package dev.akarah.cdata.registry.text;

import com.mojang.serialization.Codec;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.List;
import java.util.Optional;

public record ParsedText(
        String string,
        List<FunctionArgument.FunctionCall> interpolations
) {
    public static Codec<ParsedText> CODEC = Codec.STRING.xmap(
            Parser::parseTextLine,
            x -> { throw new RuntimeException("ummm no sorry"); }
    );

    public Optional<Component> output(Parser.Environment environment) {
        var mm = MiniMessage.miniMessage();
        System.out.println(string);
        var component = mm.deserialize(this.string, tagResolver(environment));

        var plainText = PlainTextComponentSerializer.plainText().serialize(component);
        // interpolation failed
        // this also works for %skip, since throwing an exception gets caught in the tag resolver
        if(plainText.contains("<interpolate:")) {
            return Optional.empty();
        }
        return Optional.of(component);
    }

    public TagResolver tagResolver(Parser.Environment environment) {
        return TagResolver.resolver(
                "interpolate",
                (args, ctx) -> {
                    var stringContent = this.interpolations.get(args.pop().asInt().orElse(0))
                            .evaluate(environment)
                            .toString();
                    return Tag.inserting(ctx.deserialize(stringContent));
                }
        );
    }
}
