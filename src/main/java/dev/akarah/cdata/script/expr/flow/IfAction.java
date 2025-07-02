package dev.akarah.cdata.script.expr.flow;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.List;
import java.util.Optional;

public record IfAction(
        Expression condition,
        Expression then,
        Optional<Expression> orElse
) implements Expression {

    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.condition);
        ctx.ifThenElse(
                () -> ctx.pushValue(this.then),
                () -> orElse.map(ctx::pushValue).orElse(ctx)
        );
    }

    @Override
    public Type<?> type() {
        return Type.void_();
    }

    @Override
    public int localsRequiredForCompile() {
        int a1 = this.then.localsRequiredForCompile();
        int a2 = this.orElse.map(Expression::localsRequiredForCompile).orElse(0);
        return Math.max(a1, a2);
    }
}
