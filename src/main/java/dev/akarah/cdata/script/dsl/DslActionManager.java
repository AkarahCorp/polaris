package dev.akarah.cdata.script.dsl;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.registry.Resources;
import dev.akarah.cdata.script.expr.ast.SchemaExpression;
import dev.akarah.cdata.script.expr.ast.TypeExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import dev.akarah.cdata.script.value.event.REvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

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
    Map<String, Type<?>> dslTypes = Maps.newHashMap();
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

    public MethodHandle functionByRawName(String string) {
        return this.namedMethodHandles.get(string);
    }

    public MethodHandle functionByLocation(ResourceLocation resourceLocation) {
        return this.methodHandles.get(resourceLocation);
    }

    Map<String, Type<?>> typeInterning = Maps.newHashMap();
    Map<String, List<ResourceLocation>> eventInterning = Maps.newHashMap();

    public List<ResourceLocation> functionsByEventType(String typeName) {
        var type = typeInterning.get(typeName);
        if(type == null) {
            var tokens = DslTokenizer.tokenize(ResourceLocation.fromNamespaceAndPath("_", "_"), "event[" + typeName + "]").getOrThrow();
            type = DslParser.parseTopLevelType(tokens);
        }

        var event = eventInterning.get(typeName);
        if(event == null) {
            Type<?> finalType = type;
            event = this.dslExpressions.entrySet()
                    .stream()
                    .filter(x -> !x.getValue().parameters().isEmpty())
                    .map(x -> Map.entry(x.getKey(), x.getValue().parameters().getFirst().getSecond()))
                    .filter(x -> x.getValue().typeEquals(finalType))
                    .map(Map.Entry::getKey)
                    .map(x -> this.resourceNames.get(x))
                    .toList();
        }

        return event;
    }

    public void callEvents(List<ResourceLocation> functions, REvent event) {
        for(var f : functions) {
            var m = this.functionByLocation(f);
            try {
                m.invokeWithArguments(List.of(event));
            } catch (Throwable _) {

            }
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
                            if(!(entry.getValue().getFirst() instanceof DslToken.TypeKeyword)) {
                                continue;
                            }
                            if(this.dslTypes.containsKey(entry.getKey())) {
                                continue;
                            }

                            try {
                                var expression = DslParser.parseTopLevelExpression(entry.getValue(), this.dslTypes);

                                if (expression instanceof TypeExpression(Type<?> alias)) {
                                    this.dslTypes.put(entry.getKey(), alias);
                                    counts++;
                                }
                            } catch (Exception e) {

                            }
                        }
                        if(counts == 0) {
                            break;
                        }
                    }
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
                                    .toList()
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
