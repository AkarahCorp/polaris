package dev.akarah.cdata.script.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.script.expr.bool.BooleanExpression;
import dev.akarah.cdata.script.expr.entity.EntityDirectionExpression;
import dev.akarah.cdata.script.expr.entity.EntityPositionExpression;
import dev.akarah.cdata.script.expr.entity.EntityTeleportAction;
import dev.akarah.cdata.script.expr.entity.EntityTeleportRelativeAction;
import dev.akarah.cdata.script.expr.flow.AllOfAction;
import dev.akarah.cdata.script.expr.flow.IfAction;
import dev.akarah.cdata.script.expr.number.NumberExpression;
import dev.akarah.cdata.script.expr.player.PlayerSendActionbarAction;
import dev.akarah.cdata.script.expr.player.PlayerSendMessageAction;
import dev.akarah.cdata.script.expr.string.StringExpression;
import dev.akarah.cdata.script.expr.text.TextExpression;
import dev.akarah.cdata.script.expr.vec3.*;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.TextType;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public interface Expression {
    void compile(CodegenContext ctx);
    Type<?> type();
    MapCodec<? extends Expression> generatorCodec();

    Codec<Expression> NUMBER_CODEC = Codec.DOUBLE.xmap(NumberExpression::new, x -> ((NumberExpression) x).value());
    Codec<Expression> STRING_CODEC = Codec.STRING.xmap(StringExpression::new, x -> ((StringExpression) x).value());
    Codec<Expression> TEXT_CODEC = ParsedText.CODEC
            .validate(s -> {
                if(s.string().startsWith("$")) {
                    return DataResult.success(new ParsedText(
                            s.string().replaceFirst("\\$", ""),
                            s.interpolations()
                    ));
                } else {
                    return DataResult.error(() -> "Not a text, must start with `$`");
                }
            })
            .xmap(TextExpression::new, x -> ((TextExpression) x).value());
    Codec<Expression> BOOLEAN_CODEC = Codec.BOOL.xmap(BooleanExpression::new, x -> ((BooleanExpression) x).value());

    Codec<Expression> BASE_CODEC = Codec.lazyInitialized(() -> ExtBuiltInRegistries.ACTION_TYPE
            .byNameCodec()
            .dispatch(Expression::generatorCodec, x -> x));

    Codec<Expression> CODEC = Codec.recursive(
            "expression",
            codec -> Expression.alternatives(
                    BASE_CODEC,
                    TEXT_CODEC,
                    BOOLEAN_CODEC,
                    NUMBER_CODEC,
                    STRING_CODEC,
                    codec.listOf().xmap(AllOfAction::new, x -> ((AllOfAction) x).actions())
            )
    );

    static Codec<Expression> codecByType(Type<?> type) {
        return CODEC.validate(x -> {
            if(x.type().typeEquals(type)) {
                return DataResult.success(x);
            } else {
                return DataResult.error(() -> "Expected value of type `" + type.typeName() + "` for expression " + x);
            }
        });
    }

    static Object bootStrap(Registry<MapCodec<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("all_of"), AllOfAction.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("if"), IfAction.GENERATOR_CODEC);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("string"), StringExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("text"), TextExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("number"), NumberExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("boolean"), BooleanExpression.GENERATOR_CODEC);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3"), Vec3Expression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3/add"), Vec3AddExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3/multiply"), Vec3MultiplyExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3/x"), Vec3XExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3/y"), Vec3YExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3/z"), Vec3ZExpression.GENERATOR_CODEC);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/position"), EntityPositionExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/direction"), EntityDirectionExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/teleport"), EntityTeleportAction.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/teleport_relative"), EntityTeleportRelativeAction.GENERATOR_CODEC);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("player/send_message"), PlayerSendMessageAction.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("player/send_actionbar"), PlayerSendActionbarAction.GENERATOR_CODEC);

        return StringExpression.GENERATOR_CODEC;
    }

    @SuppressWarnings("unchecked")
    static <T> Codec<T> alternatives(Codec<T>... codecs) {
        var codec = codecs[0];
        for(int i = 1; i < codecs.length; i++) {
            codec = Codec.withAlternative(
                    codec,
                    codecs[i]
            );
        }
        return codec;
    }
}
