package dev.akarah.cdata.script.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.script.expr.number.NumberExpression;
import dev.akarah.cdata.script.expr.player.PlayerSendMessageAction;
import dev.akarah.cdata.script.expr.string.StringExpression;
import dev.akarah.cdata.script.expr.text.TextExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;

public interface Expression {
    void compile(CodegenContext ctx);
    Type<?> type();
    MapCodec<? extends Expression> generatorCodec();

    Codec<Expression> NUMBER_CODEC = Codec.DOUBLE.xmap(NumberExpression::new,  x -> ((NumberExpression) x).value());
    Codec<Expression> STRING_CODEC = Codec.STRING.xmap(StringExpression::new, x -> ((StringExpression) x).value());

    Codec<Expression> BASE_CODEC = Codec.lazyInitialized(() -> ExtBuiltInRegistries.ACTION_TYPE
            .byNameCodec()
            .dispatch(Expression::generatorCodec, x -> x));

    Codec<Expression> CODEC = Codec.withAlternative(
            BASE_CODEC,
            Codec.withAlternative(
                    NUMBER_CODEC,
                    STRING_CODEC
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
        Registry.register(actions, ResourceLocation.withDefaultNamespace("string"), StringExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("text"), TextExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("number"), NumberExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("player/send_message"), PlayerSendMessageAction.GENERATOR_CODEC);
        return StringExpression.GENERATOR_CODEC;
    }
}
