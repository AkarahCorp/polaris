package dev.akarah.polaris.script.dsl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import dev.akarah.polaris.io.ExceptionPrinter;
import dev.akarah.polaris.script.exception.MultiException;
import dev.akarah.polaris.script.exception.SpanData;
import dev.akarah.polaris.script.exception.SpannedException;
import dev.akarah.polaris.script.expr.ast.SchemaExpression;
import dev.akarah.polaris.script.expr.ast.TypeExpression;
import dev.akarah.polaris.script.jvm.CodegenContext;
import dev.akarah.polaris.script.type.StructType;
import dev.akarah.polaris.script.value.RBoolean;
import dev.akarah.polaris.script.value.RuntimeValue;
import dev.akarah.polaris.script.value.polaris.DslProfiler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

public class DslActionManager {
    public static AtomicReference<DslProfiler> PROFILER = new AtomicReference<>();

    Map<ResourceLocation, SchemaExpression> dslExpressions = Maps.newHashMap();
    Map<ResourceLocation, StructType> dslTypes = Maps.newHashMap();
    Map<ResourceLocation, MethodHandle> methodHandles = Maps.newHashMap();
    Map<String, MethodHandle> namedMethodHandles = Maps.newHashMap();
    Class<?> codeClass;

    public static List<String> events() {
        return List.of(
                "server.tick(): void",
                "player.complete_achievement(player: entity, id: identifier): void",
                "player.join(player: entity): void",
                "player.quit(player: entity): void",
                "player.custom_click_action(player: entity, id: identifier, data: dict[string, any]): void",
                "player.right_click_block(entity: entity, position: vector): boolean",
                "player.close_inventory(player: entity): void",
                "player.break_block(player: entity, position: vector): boolean",
                "player.mining_rule_activation(player: entity, position: vector, rule: identifier): boolean",
                "player.send_chat_message(player: entity, message: string): boolean",
                "player.fish(player: entity): void",
                "player.hurt(entity: entity, damage: cell[number], damage_type: identifier): boolean",
                "player.swap_hands(entity: entity): void",
                "player.place_block(player: entity, position: vector): boolean",
                "item.menu_click(player: entity, item: item): boolean",
                "item.menu_click_typed(player: entity, item: item, type: string): boolean",
                "item.get_stats(item: item, entity: nullable[entity], stats: stat_obj): void",
                "entity.interact(player: entity, victim: entity): void",
                "entity.take_damage.after(victim: entity, damage: cell[number]): void",
                "entity.take_damage(victim: entity, damage: cell[number]): void",
                "projectile.attack_entity(projectile: entity, victim: entity, damage: cell[number]): boolean",
                "player.tick(player: entity): void",
                "player.stat_tick(player: entity, stats: stat_obj): void",
                "player.attack_entity(player: entity, victim: entity, damage: cell[number], type: identifier): boolean",
                "item.left_click(player: entity, item: item): void",
                "item.right_click(player: entity, item: item): void"
        );
    }


    public Map<ResourceLocation, SchemaExpression> expressions() {
        return this.dslExpressions;
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
        if(mh == null) {
            return;
        }
        try {
            mh.invokeWithArguments((Object[]) arguments);
        } catch (Throwable e) {
            if(e.getMessage() == null) {
                return;
            }
            if(e.getMessage().contains("because \"mh\" is null")) {
                return;
            }
            ExceptionPrinter.writeExceptionToOps(e, name);
        }
    }

    public boolean executeBoolean(ResourceLocation name, RuntimeValue... arguments) {
        var mh = methodHandleByLocation(name);
        if(mh == null) {
            return true;
        }
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
            ExceptionPrinter.writeExceptionToOps(e, name);
            return true;
        }
    }

    ConcurrentMap<String, List<ResourceLocation>> eventInterning = Maps.newConcurrentMap();

    public void internEventTypes() {
        if(!eventInterning.isEmpty()) {
            return;
        }
        for(var function : this.dslExpressions.entrySet()) {
            function.getValue().eventName().ifPresent(eventName -> {
                if(eventInterning.containsKey(eventName)) {
                    eventInterning.get(eventName).add(function.getKey());
                } else {
                    eventInterning.put(eventName, Lists.newArrayList(function.getKey()));
                }
            });
        }
        for(var entry : this.eventInterning.entrySet()) {
            entry.getValue().sort(
                    Comparator.comparingInt(
                            x -> {
                                var priority = Integer.MAX_VALUE;
                                var annotations = dslExpressions.get(x).annotations();
                                if(annotations.stream().anyMatch(a -> a.name().equals("event.priority.high"))) {
                                    priority = 0;
                                }
                                if(annotations.stream().anyMatch(a -> a.name().equals("event.priority.medium"))) {
                                    priority = 1;
                                }
                                if(annotations.stream().anyMatch(a -> a.name().equals("event.priority.low"))) {
                                    priority = 2;
                                }
                                return priority;
                            }
                    )
            );
        }
    }

    private List<ResourceLocation> functionsByEventType(String typeName) {
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
                    var panicking = Lists.<SpannedException>newArrayList();
                    for(var resourceEntry : resourceManager.listResources("engine/dsl", rl -> rl.getPath().endsWith(".pol")).entrySet()) {
                        try(var inputStream = resourceEntry.getValue().open()) {
                            var bytes = inputStream.readAllBytes();
                            var string = new String(bytes);

                            var key = resourceEntry.getKey().withPath(s ->
                                    s.replace(".pol", "")
                                            .replace("engine/dsl/", ""));

                            var tokens = List.<DslToken>of();
                            try {
                                tokens = DslTokenizer.tokenize(key, string).getOrThrow();
                            } catch (SpannedException e) {
                                panicking.add(e);
                            } catch (Exception e) {
                                System.out.println("Error parsing script `" + key + "`: " + e.getMessage());
                            }
                            try {
                                var expressions = DslParser.parseTopLevelExpression(tokens, this.dslTypes);

                                for(var expression : expressions) {
                                    if (expression instanceof TypeExpression(ResourceLocation name, StructType alias, SpanData spanData)) {
                                        this.dslTypes.put(name, alias);
                                    }
                                    if(expression instanceof SchemaExpression schemaExpression) {
                                        this.dslExpressions.put(schemaExpression.location(), schemaExpression);
                                    }
                                }
                            } catch (SpannedException e) {
                                panicking.add(e);
                            }


                        } catch (IOException e) {
                            this.dslExpressions.clear();
                            this.dslTypes.clear();
                            throw new RuntimeException(e);
                        }
                    }

                    if(!panicking.isEmpty()) {
                        this.dslExpressions.clear();
                        this.dslTypes.clear();
                        throw new MultiException(panicking);
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

                        for(var expr : this.dslExpressions.entrySet()) {
                            this.methodHandles.put(
                                    expr.getKey(),
                                    lookup.findStatic(
                                            codeClass,
                                            CodegenContext.resourceLocationToMethodName(expr.getKey()),
                                            expr.getValue().methodType()
                                    )
                            );
                        }
                    } catch (NoSuchMethodException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                },
                executor
        );
    }
}
