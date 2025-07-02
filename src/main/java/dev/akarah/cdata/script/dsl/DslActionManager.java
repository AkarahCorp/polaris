package dev.akarah.cdata.script.dsl;

import com.google.common.collect.Maps;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.script.expr.Expression;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DslActionManager implements IdentifiableResourceReloadListener {
    Map<ResourceLocation, String> rawDslPrograms = Maps.newHashMap();
    Map<ResourceLocation, Expression> dslExpressions = Maps.newHashMap();

    public Map<ResourceLocation, Expression> expressions() {
        return this.dslExpressions;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.withDefaultNamespace("action_manager");
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture
                .runAsync(
                        () -> {
                            for(var resourceEntry : resourceManager.listResources("engine/dsl", rl -> rl.getPath().endsWith(".aka")).entrySet()) {
                                try(var inputStream = resourceEntry.getValue().open()) {
                                    var bytes = inputStream.readAllBytes();
                                    var string = new String(bytes);
                                    this.rawDslPrograms.put(
                                            resourceEntry.getKey().withPath(s ->
                                                    s.replace(".aka", "")
                                                            .replace("engine/dsl/", "")),
                                            string
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            System.out.println(this.rawDslPrograms);
                        },
                        prepareExecutor
                )
                .thenCompose(preparationBarrier::wait)
                .thenRunAsync(
                        () -> {
                            for(var entry : this.rawDslPrograms.entrySet()) {
                                var tokens = DslTokenizer.tokenize(entry.getValue()).getOrThrow();
                                System.out.println(entry.getKey() + " => " + tokens);

                                var expression = DslParser.parseTopLevelExpression(tokens);
                                System.out.println(entry.getKey() + " => " + expression);

                                Main.actionManager().dslExpressions.put(entry.getKey(), expression);
                            }
                        },
                        applyExecutor
                );
    }
}
