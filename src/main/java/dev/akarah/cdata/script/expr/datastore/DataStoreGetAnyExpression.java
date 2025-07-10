package dev.akarah.cdata.script.expr.datastore;

import dev.akarah.cdata.db.DataStore;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.number.NumberExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.jvm.CodegenUtil;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

@SuppressWarnings("unused")
record DataStoreGetAnyExpression(
        Expression store,
        Expression key,
        Expression fallback
) implements Expression {
    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .pushValue(this.store)
                .typecheck(DataStore.class)
                .pushValue(this.key)
                .typecheck(String.class)
                .pushValue(fallback)
                .invokeVirtual(
                        CodegenUtil.ofClass(DataStore.class),
                        "get",
                        MethodTypeDesc.of(
                                CodegenUtil.ofClass(Object.class),
                                List.of(CodegenUtil.ofClass(String.class), CodegenUtil.ofClass(Object.class))
                        )
                );
    }

    public static ExpressionTypeSet parameters() {
        return ExpressionTypeSet.builder()
                .required("store", Type.store())
                .required("key", Type.string())
                .optional("fallback", Type.any())
                .returns(Type.any())
                .build();
    }
}
