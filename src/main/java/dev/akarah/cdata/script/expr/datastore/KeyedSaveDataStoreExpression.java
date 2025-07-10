package dev.akarah.cdata.script.expr.datastore;

import dev.akarah.cdata.db.DataStore;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;

import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record KeyedSaveDataStoreExpression(
        Expression key
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx.pushValue(this.key)
                .typecheck(String.class)
                .invokeStatic(
                        CodegenUtil.ofClass(DataStoreUtil.class),
                        "saveDataStoreOf",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(DataStore.class),
                                List.of(CodegenUtil.ofClass(String.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("key", Type.string())
                .returns(Type.store())
                .build();
    }
}
