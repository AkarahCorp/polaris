package dev.akarah.cdata.script.expr.dict;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.MethodTypeDesc;
import java.util.HashMap;
import java.util.List;

public record DictGetExpression(
        Expression dict,
        Expression key
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(dict)
                .typecheck(HashMap.class)
                .pushValue(key)
                .invokeVirtual(
                        CodegenUtil.ofClass(HashMap.class),
                        "get",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Object.class),
                                List.of(CodegenUtil.ofClass(Object.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("dict", e -> Type.dict(Type.var(e, "K"), Type.var(e, "V")))
                .required("key", e -> Type.var(e, "K"))
                .returns(e -> Type.var(e, "V"))
                .build();
    }
}
