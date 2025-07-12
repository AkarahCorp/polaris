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
record DataStorePutNumberExpression(
        Expression store,
        Expression key,
        Expression value
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.store)
                .typecheck(DataStore.class)
                .pushValue(this.key)
                .typecheck(String.class)
                .pushValue(this.value)
                .typecheck(Double.class)
                .invokeVirtual(
                        CodegenUtil.ofClass(DataStore.class),
                        "putNumber",
                        MethodTypeDesc.of(
                                CodegenUtil.ofVoid(),
                                List.of(CodegenUtil.ofClass(String.class), CodegenUtil.ofClass(Double.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("store", Type.store())
                .required("key", Type.string())
                .required("value", Type.number())
                .returns(Type.void_())
                .build();
    }
}
