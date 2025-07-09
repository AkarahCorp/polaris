package dev.akarah.cdata.script.expr;

import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.dict.CreateDictExpression;
import dev.akarah.cdata.script.expr.dict.DictGetExpression;
import dev.akarah.cdata.script.expr.dict.DictPutExpression;
import dev.akarah.cdata.script.expr.entity.EntityDirectionExpression;
import dev.akarah.cdata.script.expr.entity.EntityPositionExpression;
import dev.akarah.cdata.script.expr.entity.EntityTeleportAction;
import dev.akarah.cdata.script.expr.entity.EntityTeleportRelativeAction;
import dev.akarah.cdata.script.expr.id.IdentifierExpression;
import dev.akarah.cdata.script.expr.item.*;
import dev.akarah.cdata.script.expr.list.AddListExpression;
import dev.akarah.cdata.script.expr.list.CreateListExpression;
import dev.akarah.cdata.script.expr.list.GetListExpression;
import dev.akarah.cdata.script.expr.player.PlayerSendActionbarAction;
import dev.akarah.cdata.script.expr.player.PlayerSendMessageAction;
import dev.akarah.cdata.script.expr.string.StringExpression;
import dev.akarah.cdata.script.expr.text.ComponentColorExpression;
import dev.akarah.cdata.script.expr.text.ComponentLiteralFuncExpression;
import dev.akarah.cdata.script.expr.vec3.*;
import dev.akarah.cdata.script.jvm.CodegenContext;
import dev.akarah.cdata.script.params.ExpressionTypeSet;
import dev.akarah.cdata.script.type.Type;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;

public interface Expression {
    void compile(CodegenContext ctx);
    default Type<?> type(CodegenContext ctx) {
        var lookup = MethodHandles.lookup();
        try {
            return ((ExpressionTypeSet) lookup.findStatic(this.getClass(), "parameters", MethodType.methodType(ExpressionTypeSet.class))
                    .invoke())
                    .returns();
        } catch (Throwable e) {
            throw new ParsingException("[Internal Error, please report!] Default type impl not supported for " + this, this.span());
        }
    }

    default SpanData span() {
        return new SpanData(0, 0, "unknown", ResourceLocation.withDefaultNamespace("error/unspanned"));
    }

    static Object bootStrap(Registry<Class<? extends Expression>> actions) {
        Registry.register(actions, ResourceLocation.withDefaultNamespace("identifier/create"), IdentifierExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("item_stack/create"), CreateItemExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item_stack/create_templated"), CreateTemplatedItemExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item_stack/set_lore"), SetItemLoreExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item_stack/set_name"), SetItemNameExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item_stack/name"), GetItemNameExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item_stack/stat"), GetItemStatExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("text/create"), ComponentLiteralFuncExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("text/color"), ComponentColorExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("list/create"), CreateListExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("list/add"), AddListExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("list/get"), GetListExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("dict/create"), CreateDictExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("dict/put"), DictPutExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("dict/get"), DictGetExpression.class);

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

        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/send_message"), PlayerSendMessageAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/send_actionbar"), PlayerSendActionbarAction.class);

        var lookup = MethodHandles.lookup();
        var failures = new ArrayList<>();
        actions.listElements().forEach(classReference -> {
            try {
                lookup.findStatic(classReference.value(), "parameters", MethodType.methodType(ExpressionTypeSet.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                failures.add(classReference.key().location());
            }
        });
        if(!failures.isEmpty()) {
            throw new RuntimeException("The following actions lack the Expression#parameters() -> ExpressionTypeSet, " + failures);
        }

        return StringExpression.class;
    }
}
