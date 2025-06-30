package dev.akarah.cdata.script.jvm;

import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.script.action.CompilableAction;
import dev.akarah.cdata.script.env.JIT;
import dev.akarah.cdata.script.env.RuntimeContext;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

import java.lang.classfile.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.ref.Reference;
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

    public static Class<?> initializeCompilation(List<Holder.Reference<CompilableAction>> refs) {
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

    private static byte[] compileClassBytecode(List<Holder.Reference<CompilableAction>> refs) {
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

    private ClassBuilder compileAction(ResourceLocation name, CompilableAction action) {
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
}
