package dev.akarah.cdata.script.action.test;

import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.script.action.CompilableAction;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.jvm.CodegenContext;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.classfile.Opcode;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record TestAction() implements CompilableAction {
    public static MapCodec<CompilableAction> GENERATOR_CODEC = MapCodec.unit(TestAction::new);

    @Override
    public void compile(CodegenContext ctx) {
        ctx
                .bytecode(cb -> cb.getstatic(
                        JIT.ofClass(System.class),
                        "out",
                        JIT.ofClass(PrintStream.class)
                ))
                .bytecode(cb -> cb.loadConstant("Hello world!"))
                .bytecode(cb -> cb.invoke(
                        Opcode.INVOKEVIRTUAL,
                        JIT.ofClass(PrintStream.class),
                        "println",
                        MethodTypeDesc.of(
                                JIT.ofVoid(),
                                List.of(
                                        JIT.ofClass(String.class)
                                )
                        ),
                        false
                ));
    }

    @Override
    public MapCodec<CompilableAction> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
