package dev.akarah.cdata.script.expr;

import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.entity.EntityDirectionExpression;
import dev.akarah.cdata.script.expr.entity.EntityPositionExpression;
import dev.akarah.cdata.script.expr.entity.EntityTeleportAction;
import dev.akarah.cdata.script.expr.entity.EntityTeleportRelativeAction;
import dev.akarah.cdata.script.expr.player.PlayerSendActionbarAction;
import dev.akarah.cdata.script.expr.player.PlayerSendMessageAction;
import dev.akarah.cdata.script.expr.string.StringExpression;
import dev.akarah.cdata.script.expr.vec3.*;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public interface Expression {
    void compile(CodegenContext ctx);
    Type<?> type(CodegenContext ctx);

    default SpanData span() {
        return new SpanData(0, 0, "unknown", ResourceLocation.withDefaultNamespace("error/unspanned"));
    }

    default int localsRequiredForCompile() {
        return 0;
    }

    static Object bootStrap(Registry<Class<? extends Expression>> actions) {
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
