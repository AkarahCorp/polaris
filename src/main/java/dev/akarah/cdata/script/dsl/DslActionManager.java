package dev.akarah.cdata.script.dsl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.expr.ast.SchemaExpression;
import dev.akarah.cdata.script.expr.ast.TypeExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.StructType;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.RBoolean;
import dev.akarah.cdata.script.value.RuntimeValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.xml.validation.Validator;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DslActionManager {
    Map<String, String> rawDslPrograms = Maps.newHashMap();
    Map<String, List<DslToken>> rawDslTokens = Maps.newHashMap();
    Map<String, SchemaExpression> dslExpressions = Maps.newHashMap();
    Map<String, StructType> dslTypes = Maps.newHashMap();
    Map<SchemaExpression, String> dslReverseExpressions = Maps.newHashMap();
    Map<String, ResourceLocation> resourceNames = Maps.newHashMap();
    Map<ResourceLocation, MethodHandle> methodHandles = Maps.newHashMap();
    Map<String, MethodHandle> namedMethodHandles = Maps.newHashMap();
    Class<?> codeClass;

    public Map<String, SchemaExpression> expressions() {
        return this.dslExpressions;
    }

    public Map<SchemaExpression, String> reverseExpressions() {
        return this.dslReverseExpressions;
    }

    public Map<String, ResourceLocation> resourceNames() {
        return this.resourceNames;
    }

    public Class<?> codeClass() {
        return this.codeClass;
    }

    public MethodHandle methodHandleByRawName(String string) {
        return this.namedMethodHandles.get(string);
    }

    public MethodHandle methodHandleByLocation(ResourceLocation resourceLocation) {
        return this.methodHandles.get(resourceLocation);
    }

    public void executeVoid(ResourceLocation name, RuntimeValue... arguments) {
        var mh = methodHandleByLocation(name);
        try {
            mh.invokeWithArguments((Object[]) arguments);
        } catch (Throwable e) {
            if(e.getMessage() == null) {
                return;
            }
            if(e.getMessage().contains("because \"mh\" is null")) {
                return;
            }
            System.out.println("Error executing script `" + name + "`: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean executeBoolean(ResourceLocation name, RuntimeValue... arguments) {
        var mh = methodHandleByLocation(name);
        try {
            var result = mh.invokeWithArguments((Object[]) arguments);
            if(result instanceof RBoolean a) {
                return a.javaValue();
            }
            return true;
        } catch (Throwable e) {
            if(e.getMessage() == null) {
                return true;
            }
            if(e.getMessage().contains("because \"mh\" is null")) {
                return true;
            }
            System.out.println("Error executing script `" + name + "`: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    Map<String, List<ResourceLocation>> eventInterning = Maps.newHashMap();

    public void internEventTypes() {
        eventInterning.clear();
        for(var function : this.dslExpressions.entrySet()) {
            function.getValue().eventName().ifPresent(eventName -> {
                if(eventInterning.containsKey(eventName)) {
                    eventInterning.get(eventName).add(resourceNames.get(function.getKey()));
                } else {
                    eventInterning.put(eventName, Lists.newArrayList(resourceNames.get(function.getKey())));
                }
            });
        }
    }

    public List<ResourceLocation> functionsByEventType(String typeName) {
        if(eventInterning.isEmpty()) {
            internEventTypes();
        }
        var value = eventInterning.get(typeName);
        if(value == null) {
            return List.of();
        }
        return value;
    }

    public boolean performEvents(String name, RuntimeValue... parameters) {
        for(var f : functionsByEventType(name)) {
            var result = this.executeBoolean(f, parameters);
            if(!result) {
                return false;
            }
        }
        return true;
    }

    public void callFunctions(List<ResourceLocation> functions, RuntimeValue... parameters) {
        for(var f : functions) {
            this.executeVoid(f, parameters);
        }
    }

    public CompletableFuture<Void> reloadWithManager(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.runAsync(
                () -> {
                    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                    for(var resourceEntry : resourceManager.listResources("engine/dsl", rl -> rl.getPath().endsWith(".aka")).entrySet()) {
                        try(var inputStream = resourceEntry.getValue().open()) {
                            var bytes = inputStream.readAllBytes();
                            var string = new String(bytes);

                            var key = resourceEntry.getKey().withPath(s ->
                                    s.replace(".aka", "")
                                            .replace("engine/dsl/", ""));
                            var methodName = CodegenContext.resourceLocationToMethodName(key);
                            this.resourceNames.put(methodName, key);
                            this.rawDslPrograms.put(
                                    methodName,
                                    string
                            );
                        } catch (IOException e) {
                            this.rawDslPrograms.clear();
                            this.dslExpressions.clear();
                            this.dslTypes.clear();
                            throw new RuntimeException(e);
                        }
                    }

                    for(var entry : this.rawDslPrograms.entrySet()) {
                        var tokens = DslTokenizer.tokenize(this.resourceNames.get(entry.getKey()), entry.getValue()).getOrThrow();
                        this.rawDslTokens.put(entry.getKey(), tokens);
                    }

                    while(true) {
                        int counts = 0;
                        for(var entry : this.rawDslTokens.entrySet()) {
                            if(!(entry.getValue().getFirst() instanceof DslToken.StructKeyword)) {
                                continue;
                            }
                            if(this.dslTypes.containsKey(entry.getKey())) {
                                continue;
                            }

                            try {
                                var expression = DslParser.parseTopLevelExpression(entry.getValue(), this.dslTypes);

                                if (expression instanceof TypeExpression(StructType alias)) {
                                    this.dslTypes.put(entry.getKey(), alias);
                                    counts++;
                                }
                            } catch (Exception e) {
                                System.out.println("Error during type instantiation, if the server starts ignore this:");
                                System.out.println(e.getMessage());
                            }
                        }
                        if(counts == 0) {
                            break;
                        }
                    }
                    System.out.println(this.dslTypes.keySet());
                    for(var entry : this.rawDslPrograms.entrySet()) {
                        var tokens = DslTokenizer.tokenize(this.resourceNames.get(entry.getKey()), entry.getValue()).getOrThrow();
                        var expression = DslParser.parseTopLevelExpression(tokens, this.dslTypes);

                        if(expression instanceof SchemaExpression schemaExpression) {
                            this.dslExpressions.put(entry.getKey(), schemaExpression);
                        }
                    }

                    this.codeClass = CodegenContext.initializeCompilation(
                            this.dslExpressions.entrySet()
                                    .stream()
                                    .map(x -> Pair.of(x.getKey(), x.getValue()))
                                    .toList(),
                            this.dslTypes
                    );

                    var lookup = MethodHandles.lookup();

                    try {
                        this.namedMethodHandles.put("$static_init", lookup.findStatic(codeClass, "$static_init", MethodType.methodType(void.class)));

                        for(var element : this.dslExpressions.entrySet()) {
                            var resourceName = Resources.actionManager().resourceNames().get(element.getKey());
                            var methodHandle = lookup.findStatic(
                                    codeClass,
                                    element.getKey(),
                                    element.getValue().methodType()
                            );
                            this.namedMethodHandles.put(element.getKey(), methodHandle);
                            this.methodHandles.put(resourceName, methodHandle);
                        }
                    } catch (NoSuchMethodException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                },
                executor
        );
    }
}
