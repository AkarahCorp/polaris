package dev.akarah.cdata.script.expr.flow;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.List;

public record AllOfAction(
        List<Expression> actions
) implements Expression {
    public static MapCodec<AllOfAction> GENERATOR_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Expression.codecByType(Type.void_()).listOf().fieldOf("actions").forGetter(AllOfAction::actions)
    ).apply(instance, AllOfAction::new));

    @Override
    public void compile(CodegenContext ctx) {
        for(var action : this.actions) {
            ctx.pushValue(action);
        }
    }

    @Override
    public Type<?> type() {
        return Type.void_();
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }

    @Override
    public int localsRequiredForCompile() {
        int h = 0;
        for(var action : this.actions) {
            var h2 = action.localsRequiredForCompile();
            if(h2 >= h) {
                h = h2;
            }
        }
        return h;
    }
}
