package dev.akarah.cdata.script.jvm;

import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.env.RuntimeContext;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.lang.classfile.*;
import java.lang.constant.*;
import java.lang.reflect.AccessFlag;
import java.util.List;
import java.util.function.Function;

public class CodegenContext {
    public static ClassDesc ACTION_CLASS_DESC = ClassDesc.of(
            "dev.akarah.cdata.script.compiled",
            "CompiledActionCode"
    );
    public static String RAW_CLASS_NAME = "dev.akarah.cdata.script.compiled.CompiledActionCode";

    ClassBuilder classBuilder;
    MethodBuilder methodBuilder;
    CodeBuilder codeBuilder;

    public static Class<?> initializeCompilation(List<Holder.Reference<Expression>> refs) {
        var bytes = CodegenContext.compileClassBytecode(refs);
        var classLoader = new ByteClassLoader(Thread.currentThread().getContextClassLoader());
        classLoader.registerClass(RAW_CLASS_NAME, bytes);
        try {
            return classLoader.findClass(RAW_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String resourceLocationToMethodName(ResourceLocation name) {
        return name.toString().replace(":", "__");
    }

    private static byte[] compileClassBytecode(List<Holder.Reference<Expression>> refs) {
        var classFile = ClassFile.of();

        return classFile.build(
                ACTION_CLASS_DESC,
                classBuilder -> {
                    var cc = new CodegenContext();
                    cc.classBuilder = classBuilder;

                    System.out.println(refs.stream().map(Holder.Reference::key).toList());
                    refs.forEach(entry -> {
                        System.out.println("Compiling " + entry);
                        cc.classBuilder = cc.compileAction(entry.key().location(), entry.value());
                    });
                }
        );
    }

    private ClassBuilder compileAction(ResourceLocation name, Expression action) {
        return this.classBuilder.withMethod(
                resourceLocationToMethodName(name),
                MethodTypeDesc.of(JIT.ofVoid(), List.of(JIT.ofClass(RuntimeContext.class))),
                AccessFlag.STATIC.mask() + AccessFlag.PUBLIC.mask(),
                methodBuilder -> {
                    System.out.println("IM BUILDING THE METHOD AHHH");
                    this.methodBuilder = methodBuilder;
                    methodBuilder.withCode(codeBuilder -> {
                        this.codeBuilder = codeBuilder;
                        action.compile(this);
                        this.codeBuilder.return_();
                    });
                }
        );
    }

    public CodegenContext bytecode(Function<CodeBuilder, CodeBuilder> function) {
        this.codeBuilder = function.apply(this.codeBuilder);
        return this;
    }

    public CodegenContext pushSelectedEntity() {
        this.codeBuilder.aload(0);
        this.codeBuilder.invokevirtual(
            JIT.ofClass(RuntimeContext.class),
            "primaryEntity",
            MethodTypeDesc.of(JIT.ofClass(Entity.class), List.of())
        );
        return this;
    }

    public CodegenContext pushSelectedEntityAs(ClassDesc classDesc) {
        this.codeBuilder.aload(0);
        this.codeBuilder.invokevirtual(
                JIT.ofClass(RuntimeContext.class),
                "primaryEntity",
                MethodTypeDesc.of(JIT.ofClass(Entity.class), List.of())
        );
        this.codeBuilder.checkcast(classDesc);
        return this;
    }

    public CodegenContext pushValue(Expression expression) {
        expression.compile(this);
        return this;
    }

    public CodegenContext ifSelectionIsType(
            ClassDesc target,
            Function<CodegenContext, CodegenContext> function
    ) {
        this.pushSelectedEntity();
        codeBuilder.instanceOf(target);
        codeBuilder.ifThen(
                blockCodeBuilder -> {
                    var oldBuilder = this.codeBuilder;
                    this.codeBuilder = blockCodeBuilder;

                    function.apply(this);

                    this.codeBuilder = oldBuilder;
                }
        );
        return this;
    }

    public CodegenContext invokeFromSelection(
            ClassDesc target,
            String methodName,
            ClassDesc outputType,
            List<ClassDesc> parameterTypes
    ) {
        this.codeBuilder.invokevirtual(
                target,
                methodName,
                MethodTypeDesc.of(outputType, parameterTypes)
        );
        return this;
    }
}
