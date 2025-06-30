package dev.akarah.cdata.script.expr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.script.expr.test.PlayerSendMessageAction;
import dev.akarah.cdata.script.expr.test.StringExpression;
import dev.akarah.cdata.script.expr.test.TextExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public interface Expression {
    void compile(CodegenContext ctx);
    MapCodec<? extends Expression> generatorCodec();

    Codec<Expression> CODEC = Codec.lazyInitialized(() -> ExtBuiltInRegistries.ACTION_TYPE
            .byNameCodec()
            .dispatch(Expression::generatorCodec, x -> x));

    static Object bootStrap(Registry<MapCodec<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("string"), StringExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("text"), TextExpression.GENERATOR_CODEC);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("player/send_message"), PlayerSendMessageAction.GENERATOR_CODEC);
        return StringExpression.GENERATOR_CODEC;
    }
}
