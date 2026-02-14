package dev.akarah.polaris.devext;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.polaris.registry.Resources;
import dev.akarah.polaris.script.dsl.DslActionManager;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.Type;

import java.util.ArrayList;
import java.util.List;

public class DevExtensionStatics {
    public record PrimitiveType(
            String label
    ) {
        public static Codec<PrimitiveType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("label").forGetter(PrimitiveType::label)
        ).apply(instance, PrimitiveType::new));
    }
    public record UserStruct(
            String label
    ) {
        public static Codec<UserStruct> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("label").forGetter(UserStruct::label)
        ).apply(instance, UserStruct::new));
    }

    public record EventSignature(
            String signature
    ) {
        public static Codec<EventSignature> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("signature").forGetter(EventSignature::signature)
        ).apply(instance, EventSignature::new));
    }

    public record PrimitiveFunction(
            String label,
            String writes,
            String signature,
            String documentation
    ) {
        public static Codec<PrimitiveFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("label").forGetter(PrimitiveFunction::label),
                Codec.STRING.fieldOf("writes").forGetter(PrimitiveFunction::writes),
                Codec.STRING.fieldOf("signature").forGetter(PrimitiveFunction::signature),
                Codec.STRING.fieldOf("documentation").forGetter(PrimitiveFunction::documentation)
        ).apply(instance, PrimitiveFunction::new));
    }

    public record DataBundle(
            List<PrimitiveType> types,
            List<PrimitiveFunction> functions,
            List<EventSignature> events,
            List<UserStruct> user_structs
    ) {
        public static Codec<DataBundle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PrimitiveType.CODEC.listOf().fieldOf("types").forGetter(DataBundle::types),
                PrimitiveFunction.CODEC.listOf().fieldOf("functions").forGetter(DataBundle::functions),
                EventSignature.CODEC.listOf().fieldOf("events").forGetter(DataBundle::events),
                UserStruct.CODEC.listOf().fieldOf("user_structs").forGetter(DataBundle::user_structs)
        ).apply(instance, DataBundle::new));
    }

    public static DataBundle DATA_BUNDLE;

    public static void prepareDataBundle(CodegenContext ctx) {
        var functions = new ArrayList<PrimitiveFunction>();
        for(var clazz : Type.allRVClasses()) {
            for(var method : clazz.getDeclaredMethods()) {
                try {
                    var typeName = (String) clazz.getMethod("typeName").invoke(null);
                    var annot = (MethodTypeHint) method.getAnnotations()[0];
                    functions.add(new PrimitiveFunction(
                            typeName + "." + method.getName().replace("__", "."),
                            method.getName().replace("__", "."),
                            annot.signature(),
                            annot.documentation()
                    ));
                } catch (Exception e) {

                }
            }
        }

        for(var action : Resources.actionManager().expressions().entrySet()) {
            if(!action.getKey().getNamespace().equals("lambda") && !action.getKey().getNamespace().equals("minecraft")) {
                functions.add(new PrimitiveFunction(
                        action.getKey().getNamespace() + "." + action.getKey().getPath().replace("/", "."),
                        action.getKey().getNamespace() + "." + action.getKey().getPath().replace("/", "."),
                        action.getValue().typeSet().toString(),
                        "Function implemented in userspace."
                ));
            }
        }


        var types = new ArrayList<PrimitiveType>();
        for(var type : Type.allTypes()) {
            types.add(new PrimitiveType(
                    type.verboseTypeName()
            ));
        }

        var events = new ArrayList<EventSignature>();
        for(var event : DslActionManager.events()) {
            events.add(new EventSignature(event));
        }

        var users = new ArrayList<UserStruct>();
        for(var struct : ctx.userTypes.entrySet()) {
            users.add(new UserStruct("$" + struct.getKey().toString()));
            types.add(new PrimitiveType("$" + struct.getKey().toString()));
        }

        DATA_BUNDLE = new DataBundle(types, functions, events, users);
    }
}
