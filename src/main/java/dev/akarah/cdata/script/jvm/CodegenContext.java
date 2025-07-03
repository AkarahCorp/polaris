package dev.akarah.cdata.script.jvm;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.text.ParseContext;
import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.env.RuntimeContext;
import dev.akarah.cdata.script.expr.flow.SchemaExpression;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.classfile.*;
import java.lang.constant.*;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * CodegenContext is the main class responsible for turning Actions into valid JVM bytecode.
 */
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

    Map<String, Integer> methodLocals = Maps.newHashMap();
    Map<String, Type<?>> methodLocalTypes = Maps.newHashMap();

    int localIndex = 0;

    public static CodegenContext INSTANCE;

    /**
     * Begin compiling expressions into a valid class. This method should only be called once.
     * @param refs The list of expressions to compile.
     * @return The created class.
     */
    public static Class<?> initializeCompilation(List<Pair<String, SchemaExpression>> refs) {
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

    /**
     * Converts a resource location to a valid method name.
     * @param name The resource location to convert.
     * @return The converted method name.
     */
    public static String resourceLocationToMethodName(ResourceLocation name) {
        return name.toString().replace("minecraft:", "").replace(":", "_").replace("/", "_");
    }

    /**
     * Handles the overarching transformations of actions into a class file.
     * @param refs The references to include in the transformation.
     * @return The raw bytes of the new class created.
     */
    private static byte[] compileClassBytecode(List<Pair<String, SchemaExpression>> refs) {
        var classFile = ClassFile.of();

        return classFile.build(
                ACTION_CLASS_DESC,
                classBuilder -> {
                    var cc = new CodegenContext();
                    CodegenContext.INSTANCE = cc;
                    cc.classBuilder = classBuilder;

                    refs.forEach(entry -> {
                        System.out.println("Compiling entry: " + entry.getSecond());
                        cc.classBuilder = cc.compileAction(entry.getFirst(), entry.getSecond());
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
//                                        codeBuilder.getstatic(
//                                                JIT.ofClass(System.class),
//                                                "out",
//                                                JIT.ofClass(PrintStream.class)
//                                        );
//                                        codeBuilder.loadConstant("Putting static of ID `" + entry + "`...");
//                                        codeBuilder.invokevirtual(
//                                                JIT.ofClass(PrintStream.class),
//                                                "println",
//                                                MethodTypeDesc.of(
//                                                        JIT.ofVoid(),
//                                                        List.of(JIT.ofClass(String.class))
//                                                )
//                                        );
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

    public static ResourceLocation idName(String name) {
        return ResourceLocation.withDefaultNamespace(name.replace(".", "/"));
    }

    /**
     * Compiles an individual entry in the action registry into the class.
     * @param name The name of the entry.
     * @param action The action code of the entry.
     * @return This.
     */
    private ClassBuilder compileAction(String name, SchemaExpression action) {
        var returnType = action.returnType().classDescType();
        var parameters = new ArrayList<ClassDesc>();
        parameters.add(JIT.ofClass(RuntimeContext.class));
        for(var parameter : action.parameters()) {
            parameters.add(parameter.getSecond().classDescType());
        }

        return this.classBuilder.withMethod(
                name,
                MethodTypeDesc.of(returnType, parameters),
                AccessFlag.STATIC.mask() + AccessFlag.PUBLIC.mask(),
                methodBuilder -> {
                    this.methodLocalTypes.clear();
                    this.methodLocals.clear();

                    int idx = 1;
                    for(var parameter : action.parameters()) {
                        this.methodLocals.put(parameter.getFirst(), idx++);
                        this.methodLocalTypes.put(parameter.getFirst(), parameter.getSecond());
                    }

                    this.methodBuilder = methodBuilder;
                    methodBuilder.withCode(codeBuilder -> {
                        this.codeBuilder = codeBuilder;
                        action.compile(this);
                        this.codeBuilder.return_();
                    });
                }
        );
    }

    /**
     * Exposes the underlying CodeBuilder for use by {@link Expression}.
     * @param function The function to apply.
     * @return This.
     */
    public CodegenContext bytecode(Function<CodeBuilder, CodeBuilder> function) {
        this.codeBuilder = function.apply(this.codeBuilder);
        return this;
    }

    /**
     * Exposes the underlying CodeBuilder for use by {@link Expression}.
     * @return This.
     */
    public CodeBuilder bytecode() {
        return this.codeBuilder;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Pushes the selected entity onto the stack.
     * @return This.
     */
    public CodegenContext pushSelectedEntity() {
        this.codeBuilder.aload(0);
        this.codeBuilder.invokevirtual(
            JIT.ofClass(RuntimeContext.class),
            "primaryEntity",
            MethodTypeDesc.of(JIT.ofClass(Entity.class), List.of())
        );
        return this;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Prints out a debug message at runtime.
     * @return This.
     */
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

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Pushes the selected entity onto the stack as a specific class, throwing an exception at runtime if it fails.
     * @return This.
     */
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

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Pushes an Expression onto the stack.
     * @return This.
     */
    public CodegenContext pushValue(Expression expression) {
        expression.compile(this);
        return this;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Evaluates the {@link ParsedText} on top of the stack.
     * @return This.
     */
    public CodegenContext evaluateParsedTextOrNull(Expression expression) {
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
        return this;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * At runtime, evaluates the provided block if the entity selection is of the given type.
     * @return This.
     */
    public CodegenContext ifSelectionIsType(
            ClassDesc target,
            Supplier<CodegenContext> function
    ) {
        this.pushSelectedEntity();
        codeBuilder.instanceOf(target);
        codeBuilder.ifThen(
                blockCodeBuilder -> {
                    var oldBuilder = this.codeBuilder;
                    this.codeBuilder = blockCodeBuilder;

                    function.get();

                    this.codeBuilder = oldBuilder;
                }
        );
        return this;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * At runtime, invokes a method on the selected entity.
     * @return This.
     */
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

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Generates a random name for a static variable.
     * @return This.
     */
    public String randomName() {
        return "static_" + UUID.randomUUID().toString().replace("-", "_");
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Generates a random name for a static variable, with a specified prefix.
     * @return This.
     */
    public String randomName(String base) {
        return base + "_" + randomName();
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Generates a new static field, useful for holding compile-time-known constants.
     * @return This.
     */
    public CodegenContext createStatic(String name, Class<?> type, Object value) {
        this.staticClasses.put(name, type);
        this.staticValues.put(name, value);
        return this;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Loads a static of a name and type onto the stack.
     * @return This.
     */
    public CodegenContext loadStatic(String name, Class<?> type) {
        this.codeBuilder.getstatic(
                CodegenContext.ACTION_CLASS_DESC,
                name,
                JIT.ofClass(type)
        );
        return this;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Turns a `double` into a `Double` at runtime.
     * @return This.
     */
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

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Turns a `Double` into a `double` at runtime.
     * @return This.
     */
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

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Gets the component of a Vec3. Accepts "x", "y", or "z".
     * @return This.
     */
    public CodegenContext getVectorComponent(String component) {
        this.codeBuilder.getfield(
                JIT.ofClass(Vec3.class),
                component,
                JIT.ofDouble()
        );
        return this;
    }

    public CodegenContext runIfNonNull(Supplier<CodegenContext> function) {
        codeBuilder.dup();
        codeBuilder.aconst_null();

        codeBuilder.ifThenElse(
                Opcode.IF_ACMPEQ,
                blockCodeBuilder -> {
                    var oldBuilder = this.codeBuilder;
                    this.codeBuilder = blockCodeBuilder;

                    function.get();

                    this.codeBuilder = oldBuilder;
                },
                CodeBuilder::pop
        );
        return this;
    }

    public CodegenContext runIfNonNull(Supplier<CodegenContext> function, Supplier<CodegenContext> orElse) {
        codeBuilder.dup();
        codeBuilder.aconst_null();

        codeBuilder.ifThenElse(
                Opcode.IF_ACMPNE,
                blockCodeBuilder -> {
                    var oldBuilder = this.codeBuilder;
                    this.codeBuilder = blockCodeBuilder;

                    function.get();

                    this.codeBuilder = oldBuilder;
                },
                blockCodeBuilder -> {
                    blockCodeBuilder.pop();
                    var oldBuilder = this.codeBuilder;
                    this.codeBuilder = blockCodeBuilder;

                    orElse.get();

                    this.codeBuilder = oldBuilder;
                }
        );
        return this;
    }

    public CodegenContext ifThen(Opcode opcode, Supplier<CodegenContext> function) {
        codeBuilder.ifThen(
                opcode,
                blockCodeBuilder -> {
                    var oldBuilder = this.codeBuilder;
                    this.codeBuilder = blockCodeBuilder;

                    function.get();

                    this.codeBuilder = oldBuilder;
                }
        );
        return this;
    }

    public CodegenContext ifThen(Supplier<CodegenContext> function) {
        codeBuilder.ifThen(
                blockCodeBuilder -> {
                    var oldBuilder = this.codeBuilder;
                    this.codeBuilder = blockCodeBuilder;

                    function.get();

                    this.codeBuilder = oldBuilder;
                }
        );
        return this;
    }

    public CodegenContext ifThenElse(Supplier<CodegenContext> function, Supplier<CodegenContext> orElse) {
        codeBuilder.ifThenElse(
                blockCodeBuilder -> {
                    var oldBuilder = this.codeBuilder;
                    this.codeBuilder = blockCodeBuilder;

                    function.get();

                    this.codeBuilder = oldBuilder;
                },
                blockCodeBuilder -> {
                    var oldBuilder = this.codeBuilder;
                    this.codeBuilder = blockCodeBuilder;

                    orElse.get();

                    this.codeBuilder = oldBuilder;
                }
        );
        return this;
    }

    public Type<?> getTypeOf(Expression expression) {
        return expression.type(this);
    }

    public CodegenContext storeLocal(String variable, Type<?> type) {
        if(this.methodLocals.containsKey(variable)) {
            this.methodLocalTypes.put(variable, type);
            return this.bytecode(cb -> cb.astore(this.methodLocals.get(variable)));
        } else {
            var index = this.codeBuilder.allocateLocal(type.classFileType());
            this.methodLocals.put(variable, index);
            this.methodLocalTypes.put(variable, type);
            return this.bytecode(cb -> cb.storeLocal(type.classFileType(), index));
        }
    }

    public CodegenContext pushLocal(String variable) {
        if(!this.methodLocals.containsKey(variable)) {
            throw new RuntimeException("Variable `" + variable + "` in method doesn't exist yet!");
        }
        if(!this.methodLocalTypes.containsKey(variable)) {
            throw new RuntimeException("Variable `" + variable + "` somehow doesn't have a type!");
        }
        return this.bytecode(cb -> cb.loadLocal(this.methodLocalTypes.get(variable).classFileType(), this.methodLocals.get(variable)));
    }

    public Type<?> typeOfLocal(String variable) {
        if(!this.methodLocalTypes.containsKey(variable)) {
            throw new RuntimeException("uh idk type of " + variable);
        }
        return this.methodLocalTypes.get(variable);
    }
}
