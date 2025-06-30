package dev.akarah.cdata.script.expr.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.jvm.CodegenContext;

import java.io.PrintStream;
import java.lang.classfile.Opcode;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record StringExpression(String value) implements Expression {
    public static MapCodec<StringExpression> GENERATOR_CODEC = Codec.STRING.fieldOf("value").xmap(StringExpression::new, StringExpression::value);

    @Override
    public void compile(CodegenContext ctx) {
        ctx.bytecode(cb -> cb.loadConstant(this.value));
    }

    @Override
    public MapCodec<? extends Expression> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
