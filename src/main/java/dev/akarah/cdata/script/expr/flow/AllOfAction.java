package dev.akarah.cdata.script.expr.flow;

import com.google.common.collect.Lists;
import dev.akarah.cdata.script.exception.MultiException;
import dev.akarah.cdata.script.exception.SpannedException;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;

import java.util.List;

public record AllOfAction(
        List<Expression> actions
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        List<SpannedException> exceptions = Lists.newArrayList();
        for(var action : this.actions) {
            try {
                ctx.pushValue(action);
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
