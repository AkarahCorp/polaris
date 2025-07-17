package dev.akarah.cdata.script.jvm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.Expression;
import dev.akarah.cdata.script.expr.ast.SchemaExpression;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.type.VoidType;
import dev.akarah.cdata.script.value.RNumber;
import dev.akarah.cdata.script.value.RuntimeValue;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.classfile.*;
import java.lang.constant.*;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
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

    List<StackFrame> stackFrames = Lists.newArrayList();

    public record SchemaRequest(
            String name,
            SchemaExpression schema,
            Integer freeLocals,
            List<StackFrame> stackFrames
    ) {

    }

    List<SchemaRequest> requestedSchemas = Lists.newArrayList();

    public record StackFrame(
            Map<String, Integer> methodLocals,
            Map<String, Type<?>> methodLocalTypes,
            Label startLabel,
            Label breakLabel
    ) {

    }

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

                    refs.forEach(entry -> cc.classBuilder = cc.compileAction(entry.getFirst(), entry.getSecond(), -1, Lists.newArrayList()));
                    while(!cc.requestedSchemas.isEmpty()) {
                        var oldSchemas = cc.requestedSchemas.stream().toList();
                        cc.requestedSchemas.clear();
                        oldSchemas.forEach(entry -> cc.classBuilder = cc.compileAction(entry.name(), entry.schema(), entry.freeLocals(), entry.stackFrames()));
                    }

                    for(var field : cc.staticClasses.entrySet()) {
                        cc.classBuilder = cc.classBuilder.withField(
                                field.getKey(),
                                CodegenUtil.ofClass(field.getValue()),
                                fb -> fb.withFlags(AccessFlag.PUBLIC, AccessFlag.STATIC)
                        );
                    }

                    cc.classBuilder = cc.classBuilder.withMethod(
                            "$static_init",
                            MethodTypeDesc.of(CodegenUtil.ofVoid()),
                            AccessFlag.PUBLIC.mask() | AccessFlag.STATIC.mask(),
                            methodBuilder -> {
                                methodBuilder.withCode(codeBuilder -> {
                                    for(var entry : cc.staticClasses.keySet()) {
                                        codeBuilder.getstatic(
                                                CodegenUtil.ofClass(CodegenContext.class),
                                                "INSTANCE",
                                                CodegenUtil.ofClass(CodegenContext.class)
                                        );
                                        codeBuilder.getfield(
                                                CodegenUtil.ofClass(CodegenContext.class),
                                                "staticValues",
                                                CodegenUtil.ofClass(Map.class)
                                        );
                                        codeBuilder.loadConstant(entry);
                                        codeBuilder.invokeinterface(
                                                CodegenUtil.ofClass(Map.class),
                                                "get",
                                                MethodTypeDesc.of(
                                                        CodegenUtil.ofClass(Object.class),
                                                        List.of(CodegenUtil.ofClass(Object.class))
                                                )
                                        );

                                        var reqClass = cc.staticClasses.get(entry);
                                        codeBuilder.checkcast(CodegenUtil.ofClass(reqClass));
                                        codeBuilder.putstatic(
                                                ACTION_CLASS_DESC,
                                                entry,
                                                CodegenUtil.ofClass(reqClass)
                                        );
                                    }
                                    codeBuilder.return_();
                                });
                            }
                    );
                }
        );
    }

    public void requestAction(String name, SchemaExpression action, int highestLocal, List<StackFrame> stackFrames) {
        this.requestedSchemas.add(new SchemaRequest(
                name,
                action,
                highestLocal,
                Lists.newArrayList(stackFrames)
        ));
    }

    public int highestLocal() {
        int i = -1;
        for(var frame : this.stackFrames) {
            for(var local : frame.methodLocals.values()) {
                if(i <= local) {
                    i = local;
                }
            }
        }
        return i;
    }

    /**
     * Compiles an individual entry in the action registry into the class.
     * @param name The name of the entry.
     * @param action The action code of the entry.
     * @return This.
     */
    public ClassBuilder compileAction(String name, SchemaExpression action, int freeLocals, List<StackFrame> frames) {
        var returnType = action.returnType().flatten() instanceof VoidType ? void.class : RuntimeValue.class;
        var parameters = new ArrayList<ClassDesc>();
        for(int i = 0; i <= freeLocals; i++) {
            parameters.add(CodegenUtil.ofClass(RuntimeValue.class));
        }
        for(var parameter : action.parameters()) {
            parameters.add(parameter.getSecond().classDescType());
        }

        return this.classBuilder.withMethod(
                name,
                MethodTypeDesc.of(CodegenUtil.ofClass(returnType), parameters),
                AccessFlag.STATIC.mask() + AccessFlag.PUBLIC.mask(),
                methodBuilder -> {
                    this.stackFrames = frames;

                    this.methodBuilder = methodBuilder;
                    methodBuilder.withCode(codeBuilder -> {

                        var startLabel = codeBuilder.newLabel();
                        var endLabel = codeBuilder.newLabel();

                        this.pushFrame(startLabel, endLabel);

                        int idx = freeLocals + 1;
                        if(freeLocals == -1) {
                            idx = 0;
                        }
                        for(var parameter : action.parameters()) {
                            this.stackFrames.getLast().methodLocals.put(parameter.getFirst(), idx++);
                            this.stackFrames.getLast().methodLocalTypes.put(parameter.getFirst(), parameter.getSecond());
                        }

                        this.codeBuilder = codeBuilder;
                        codeBuilder.labelBinding(startLabel);
                        action.compile(this);
                        codeBuilder.labelBinding(endLabel);
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
    public CodegenContext bytecodeUnsafe(Function<CodeBuilder, CodeBuilder> function) {
        this.codeBuilder = function.apply(this.codeBuilder);
        return this;
    }

    /**
     * Exposes the underlying CodeBuilder for use by {@link Expression}.
     * @return This.
     */
    public CodeBuilder bytecodeUnsafe() {
        return this.codeBuilder;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Prints out a debug message at runtime.
     * @return This.
     */
    public CodegenContext log(String message) {
        this.codeBuilder.getstatic(
                CodegenUtil.ofClass(System.class),
                "out",
                CodegenUtil.ofClass(PrintStream.class)
        );
        this.codeBuilder.loadConstant(message);
        this.codeBuilder.invokevirtual(
                CodegenUtil.ofClass(PrintStream.class),
                "println",
                MethodTypeDesc.of(
                        CodegenUtil.ofVoid(),
                        CodegenUtil.ofClass(String.class)
                )
        );
        return this;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Pushes an Expression onto the stack.
     * @return This.
     */
    public CodegenContext pushValue(Expression expression) {
        if(expression == null) {
            this.codeBuilder.aconst_null();
            return this;
        }
        expression.compile(this);
        return this;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Pushes an Expression onto the stack.
     * @return This.
     */
    public CodegenContext constant(ConstantDesc desc) {
        this.codeBuilder.loadConstant(desc);
        return this;
    }

    /**
     * Used by {@link Expression#compile(CodegenContext)}.
     * Pushes an Expression onto the stack.
     * @return This.
     */
    public CodegenContext constant(int desc) {
        this.codeBuilder.loadConstant(desc);
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
                CodegenUtil.ofClass(type)
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
                CodegenUtil.ofClass(RNumber.class),
                "of",
                MethodTypeDesc.of(
                        CodegenUtil.ofClass(RNumber.class),
                        List.of(CodegenUtil.ofDouble())
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
                CodegenUtil.ofClass(RNumber.class),
                "doubleValue",
                MethodTypeDesc.of(
                        CodegenUtil.ofDouble(),
                        List.of()
                )
        );
        return this;
    }

    public CodegenContext typecheck(Class<?> expected) {
        this.codeBuilder.checkcast(CodegenUtil.ofClass(expected));
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

    public CodegenContext ifThenElse(Opcode opcode, Supplier<CodegenContext> function, Supplier<CodegenContext> orElse) {
        codeBuilder.ifThenElse(
                opcode,
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
        if(expression == null) {
            return Type.any();
        }
        return expression.type(this);
    }

    public CodegenContext storeLocal(String variable, Type<?> type) {
        for(var frame : this.stackFrames.reversed()) {
            if(frame.methodLocals.containsKey(variable)) {
                frame.methodLocalTypes.put(variable, type);
                return this.bytecodeUnsafe(cb -> cb.storeLocal(type.classFileType(), frame.methodLocals.get(variable)));
            }
        }
        var index = this.codeBuilder.allocateLocal(type.classFileType());
        var frame = this.stackFrames.getLast();
        frame.methodLocals.put(variable, index);
        frame.methodLocalTypes.put(variable, type);
        return this.bytecodeUnsafe(cb -> cb.storeLocal(type.classFileType(), index));
    }

    public CodegenContext pushLocal(String variable, SpanData spanData) {
        for(var frame : this.stackFrames.reversed()) {
            if(!frame.methodLocals.containsKey(variable)) {
                continue;
            }
            if(!frame.methodLocalTypes.containsKey(variable)) {
                continue;
            }
            return this.bytecodeUnsafe(cb -> cb.loadLocal(frame.methodLocalTypes.get(variable).classFileType(), frame.methodLocals.get(variable)));
        }
        throw new ParsingException("Variable `" + variable + "` in method doesn't exist yet!", spanData);

    }

    public CodegenContext pushFrame(Label startLabel, Label endLabel) {
        this.stackFrames.add(new StackFrame(
                Maps.newHashMap(),
                Maps.newHashMap(),
                startLabel,
                endLabel
        ));
        return this;
    }

    public CodegenContext popFrame() {
        this.stackFrames.removeLast();
        return this;
    }

    public StackFrame getFrame() {
        return this.stackFrames.getLast();
    }

    public List<StackFrame> getFrames() {
        return this.stackFrames;
    }

    public CodegenContext invokeVirtual(ClassDesc owner, String functionName, MethodTypeDesc desc) {
        this.codeBuilder.invokevirtual(
                owner,
                functionName,
                desc
        );
        return this;
    }

    public CodegenContext invokeInterface(ClassDesc owner, String functionName, MethodTypeDesc desc) {
        this.codeBuilder.invokeinterface(
                owner,
                functionName,
                desc
        );
        return this;
    }

    public CodegenContext invokeStatic(ClassDesc owner, String functionName, MethodTypeDesc desc) {
        this.codeBuilder.invokestatic(
                owner,
                functionName,
                desc
        );
        return this;
    }

    public CodegenContext aload(int index) {
        this.codeBuilder.aload(index);
        return this;
    }

    public CodegenContext iload(int index) {
        this.codeBuilder.iload(index);
        return this;
    }

    public CodegenContext astore(int index) {
        this.codeBuilder.astore(index);
        return this;
    }

    public CodegenContext istore(int index) {
        this.codeBuilder.istore(index);
        return this;
    }

    public CodegenContext dup() {
        this.codeBuilder.dup();
        return this;
    }

    public CodegenContext pop() {
        this.codeBuilder.pop();
        return this;
    }

    public CodegenContext d2i() {
        this.codeBuilder.d2i();
        return this;
    }

    public Type<?> typeOfLocal(String variable, SpanData span) {
        for(var frame : this.stackFrames) {
            if(!frame.methodLocalTypes.containsKey(variable)) {
                continue;
            }
            return frame.methodLocalTypes.get(variable);
        }
        throw new ParsingException("Could not determine type of local `" + variable + "`, perhaps it was used before it's definition?", span);

    }
}
