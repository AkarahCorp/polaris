package dev.akarah.cdata.script.expr;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import dev.akarah.cdata.registry.ExtBuiltInRegistries;
import dev.akarah.cdata.registry.text.ParsedText;
import dev.akarah.cdata.script.expr.bool.BooleanExpression;
import dev.akarah.cdata.script.expr.entity.EntityDirectionExpression;
import dev.akarah.cdata.script.expr.entity.EntityPositionExpression;
import dev.akarah.cdata.script.expr.entity.EntityTeleportAction;
import dev.akarah.cdata.script.expr.entity.EntityTeleportRelativeAction;
import dev.akarah.cdata.script.expr.flow.AllOfAction;
import dev.akarah.cdata.script.expr.flow.GetLocalAction;
import dev.akarah.cdata.script.expr.flow.IfAction;
import dev.akarah.cdata.script.expr.flow.SetLocalAction;
import dev.akarah.cdata.script.expr.number.NumberExpression;
import dev.akarah.cdata.script.expr.player.PlayerSendActionbarAction;
import dev.akarah.cdata.script.expr.player.PlayerSendMessageAction;
import dev.akarah.cdata.script.expr.string.StringExpression;
import dev.akarah.cdata.script.expr.text.TextExpression;
import dev.akarah.cdata.script.expr.vec3.*;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public interface Expression {
    void compile(CodegenContext ctx);
    Type<?> type();

    default int localsRequiredForCompile() {
        return 0;
    }

    static Object bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("all_of"), AllOfAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("if"), IfAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("local/set"), SetLocalAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("local/get"), GetLocalAction.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("string"), StringExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("text"), TextExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("number"), NumberExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("boolean"), BooleanExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3"), Vec3Expression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3/add"), Vec3AddExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3/multiply"), Vec3MultiplyExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3/x"), Vec3XExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3/y"), Vec3YExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec3/z"), Vec3ZExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/position"), EntityPositionExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/direction"), EntityDirectionExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/teleport"), EntityTeleportAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/teleport_relative"), EntityTeleportRelativeAction.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("player/send_message"), PlayerSendMessageAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("player/send_actionbar"), PlayerSendActionbarAction.class);

        return StringExpression.class;
    }
}
