package dev.akarah.cdata.script.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.script.action.test.TestAction;
import dev.akarah.cdata.script.jvm.CodegenContext;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public interface CompilableAction {
    void compile(CodegenContext ctx);
    MapCodec<CompilableAction> generatorCodec();

    Codec<CompilableAction> CODEC = Codec.lazyInitialized(() -> ExtBuiltInRegistries.ACTION_TYPE
            .byNameCodec()
            .dispatch(CompilableAction::generatorCodec, x -> x));

    static Object bootStrap(Registry<MapCodec<? extends CompilableAction>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("test_action"), TestAction.GENERATOR_CODEC);
        return TestAction.GENERATOR_CODEC;
    }
}
