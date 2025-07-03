package dev.akarah.cdata.script.dsl;

import com.google.common.collect.Maps;
import dev.akarah.cdata.registry.ExtReloadableResources;
import dev.akarah.cdata.script.expr.flow.SchemaExpression;
import dev.akarah.cdata.script.jvm.CodegenContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DslActionManager {
    Map<String, String> rawDslPrograms = Maps.newHashMap();
    Map<String, SchemaExpression> dslExpressions = Maps.newHashMap();
    Map<String, ResourceLocation> resourceNames = Maps.newHashMap();

    public Map<String, SchemaExpression> expressions() {
        return this.dslExpressions;
    }

    public Map<String, ResourceLocation> resourceNames() {
        return this.resourceNames;
    }

    public CompletableFuture<Void> reloadWithManager(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.runAsync(
                () -> {
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
                            throw new RuntimeException(e);
                        }
                    }

                    for(var entry : this.rawDslPrograms.entrySet()) {
                        var tokens = DslTokenizer.tokenize(this.resourceNames.get(entry.getKey()), entry.getValue()).getOrThrow();
                        var expression = DslParser.parseTopLevelExpression(tokens);
                        ExtReloadableResources.actionManager().dslExpressions.put(entry.getKey(), expression);
                    }
                },
                executor
        );
    }
}
