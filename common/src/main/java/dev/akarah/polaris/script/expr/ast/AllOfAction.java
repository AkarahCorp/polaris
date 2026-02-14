package dev.akarah.polaris.script.expr.ast;

import com.google.common.collect.Lists;
import dev.akarah.polaris.script.exception.MultiException;
import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.exception.SpannedException;
import dev.akarah.polaris.script.expr.Expression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;

import java.util.List;

public record AllOfAction(
        SpanData span,
        List<Expression> actions
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        List<SpannedException> exceptions = Lists.newArrayList();
        for(var action : this.actions) {
            try {
                ctx.pushValue(action);

                if(!ctx.getTypeOf(action).typeEquals(Type.void_())) {
                    throw new SpannedException(
                            "All values here must be void. Try using `_ =` at the start to ignore the value.",
                            action.span()
                    );
                }
            } catch (SpannedException e) {
                exceptions.add(e);
            }
        }
        if(!exceptions.isEmpty()) {
            throw new MultiException(exceptions);
        }
    }

    @Override
    public Type<?> type(CodegenContext ctx) {
        return Type.void_();
    }
}
