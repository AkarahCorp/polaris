package dev.akarah.cdata.script.jvm;

import com.google.common.collect.Maps;
import dev.akarah.cdata.registry.text.ParseContext;
import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.env.RuntimeContext;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.classfile.*;
import java.lang.constant.*;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class CodegenContext {
    public static ClassDesc ACTION_CLASS_DESC = ClassDesc.of(
            "dev.akarah.cdata.script.compiled",
            "CompiledActionCode"
    );
    public static String RAW_CLASS_NAME = "dev.akarah.cdata.script.compiled.CompiledActionCode";

    ClassBuilder classBuilder;
    MethodBuilder methodBuilder;
    CodeBuilder codeBuilder;
    Map<String, Class<?>> staticClasses = Maps.newHashMap();
    public Map<String, Object> staticValues = Maps.newHashMap();

    public static CodegenContext INSTANCE;

    public static Class<?> initializeCompilation(List<Holder.Reference<Expression>> refs) {
        var bytes = CodegenContext.compileClassBytecode(refs);
        try {
            Files.createDirectories(Path.of("./build/"));
            Files.write(
                    Path.of("./build/CompiledActionCode.class"),
                    bytes,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                    CodegenContext.INSTANCE = cc;
                    cc.classBuilder = classBuilder;

                    refs.forEach(entry -> {
                        cc.classBuilder = cc.compileAction(entry.key().location(), entry.value());
                    });

                    for(var field : cc.staticClasses.entrySet()) {
                        cc.classBuilder = cc.classBuilder.withField(
                                field.getKey(),
                                JIT.ofClass(field.getValue()),
                                fb -> fb.withFlags(AccessFlag.PUBLIC, AccessFlag.STATIC)
                        );
                    }

                    cc.classBuilder = cc.classBuilder.withMethod(
                            "$static_init",
                            MethodTypeDesc.of(JIT.ofVoid()),
                            AccessFlag.PUBLIC.mask() | AccessFlag.STATIC.mask(),
                            methodBuilder -> {
                                methodBuilder.withCode(codeBuilder -> {
                                    for(var entry : cc.staticClasses.keySet()) {
                                        codeBuilder.getstatic(
                                                JIT.ofClass(System.class),
                                                "out",
                                                JIT.ofClass(PrintStream.class)
                                        );
                                        codeBuilder.loadConstant("Putting static of ID `" + entry + "`...");
                                        codeBuilder.invokevirtual(
                                                JIT.ofClass(PrintStream.class),
                                                "println",
                                                MethodTypeDesc.of(
                                                        JIT.ofVoid(),
                                                        List.of(JIT.ofClass(String.class))
                                                )
                                        );
                                        codeBuilder.getstatic(
                                                JIT.ofClass(CodegenContext.class),
                                                "INSTANCE",
                                                JIT.ofClass(CodegenContext.class)
                                        );
                                        codeBuilder.getfield(
                                                JIT.ofClass(CodegenContext.class),
                                                "staticValues",
                                                JIT.ofClass(Map.class)
                                        );
                                        codeBuilder.loadConstant(entry);
                                        codeBuilder.invokeinterface(
                                                JIT.ofClass(Map.class),
                                                "get",
                                                MethodTypeDesc.of(
                                                        JIT.ofClass(Object.class),
                                                        List.of(JIT.ofClass(Object.class))
                                                )
                                        );

                                        var reqClass = cc.staticClasses.get(entry);
                                        codeBuilder.checkcast(JIT.ofClass(reqClass));
                                        codeBuilder.putstatic(
                                                ACTION_CLASS_DESC,
                                                entry,
                                                JIT.ofClass(reqClass)
                                        );
                                    }
                                    codeBuilder.return_();
                                });
                            }
                    );
                }
        );
    }

    private ClassBuilder compileAction(ResourceLocation name, Expression action) {
        return this.classBuilder.withMethod(
                resourceLocationToMethodName(name),
                MethodTypeDesc.of(JIT.ofVoid(), List.of(JIT.ofClass(RuntimeContext.class))),
                AccessFlag.STATIC.mask() + AccessFlag.PUBLIC.mask(),
                methodBuilder -> {
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

    public CodegenContext log(String message) {
        this.codeBuilder.getstatic(
                JIT.ofClass(System.class),
                "out",
                JIT.ofClass(PrintStream.class)
        );
        this.codeBuilder.loadConstant(message);
        this.codeBuilder.invokevirtual(
                JIT.ofClass(PrintStream.class),
                "println",
                MethodTypeDesc.of(
                        JIT.ofVoid(),
                        JIT.ofClass(String.class)
                )
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

    public CodegenContext evaluateParsedTextOrReturn(Expression expression) {
        expression.compile(this);
        this.codeBuilder.aload(0);
        this.codeBuilder.invokevirtual(
                JIT.ofClass(RuntimeContext.class),
                "parseContext",
                MethodTypeDesc.of(
                        JIT.ofClass(ParseContext.class),
                        List.of()
                )
        );
        this.codeBuilder.invokevirtual(
                JIT.ofClass(ParsedText.class),
                "outputOrNull",
                MethodTypeDesc.of(
                        JIT.ofClass(Component.class),
                        List.of(JIT.ofClass(ParseContext.class))
                )
        );
        this.codeBuilder.dup();
        this.codeBuilder.aconst_null();
        this.codeBuilder.ifThen(
                Opcode.IF_ACMPEQ,
                CodeBuilder::return_
        );
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

    public String randomName() {
        return "static_" + UUID.randomUUID().toString().replace("-", "_");
    }

    public String randomName(String base) {
        return base + "_" + randomName();
    }

    public CodegenContext createStatic(String name, Class<?> type, Object value) {
        this.staticClasses.put(name, type);
        this.staticValues.put(name, value);
        return this;
    }

    public CodegenContext loadStatic(String name, Class<?> type) {
        this.codeBuilder.getstatic(
                CodegenContext.ACTION_CLASS_DESC,
                name,
                JIT.ofClass(type)
        );
        return this;
    }

    public CodegenContext boxNumber() {
        this.codeBuilder.invokestatic(
                JIT.ofClass(Double.class),
                "valueOf",
                MethodTypeDesc.of(
                        JIT.ofClass(Double.class),
                        List.of(JIT.ofDouble())
                )
        );
        return this;
    }

    public CodegenContext unboxNumber() {
        this.codeBuilder.invokevirtual(
                JIT.ofClass(Double.class),
                "doubleValue",
                MethodTypeDesc.of(
                        JIT.ofDouble(),
                        List.of()
                )
        );
        return this;
    }

    public CodegenContext getVectorComponent(String component) {
        this.codeBuilder.getfield(
                JIT.ofClass(Vec3.class),
                component,
                JIT.ofDouble()
        );
        return this;
    }
}
