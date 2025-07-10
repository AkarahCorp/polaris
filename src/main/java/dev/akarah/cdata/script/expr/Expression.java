package dev.akarah.cdata.script.expr;

import dev.akarah.cdata.script.exception.ParsingException;
import dev.akarah.cdata.script.exception.SpanData;
import dev.akarah.cdata.script.expr.dict.CreateDictExpression;
import dev.akarah.cdata.script.expr.dict.DictGetExpression;
import dev.akarah.cdata.script.expr.dict.DictPutExpression;
import dev.akarah.cdata.script.expr.entity.*;
import dev.akarah.cdata.script.expr.id.IdentifierExpression;
import dev.akarah.cdata.script.expr.item.*;
import dev.akarah.cdata.script.expr.list.*;
import dev.akarah.cdata.script.expr.player.PlayerSendActionbarAction;
import dev.akarah.cdata.script.expr.player.PlayerSendMessageAction;
import dev.akarah.cdata.script.expr.string.StringExpression;
import dev.akarah.cdata.script.expr.text.ComponentColorExpression;
import dev.akarah.cdata.script.expr.text.ComponentLiteralFuncExpression;
import dev.akarah.cdata.script.expr.vec3.*;
import dev.akarah.cdata.script.expr.world.GetWorldEntitiesExpression;
import dev.akarah.cdata.script.expr.world.GetWorldExpression;
import dev.akarah.cdata.script.expr.world.WorldSetBlockExpression;
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
        Registry.register(actions, ResourceLocation.withDefaultNamespace("world/get"), GetWorldExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("world/set_block"), WorldSetBlockExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("world/entities"), GetWorldEntitiesExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("id"), IdentifierExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/create"), CreateItemExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/create_templated"), CreateTemplatedItemExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/set_lore"), SetItemLoreExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/set_name"), SetItemNameExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/name"), GetItemNameExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("item/stat"), GetItemStatExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("text"), ComponentLiteralFuncExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("text/color"), ComponentColorExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("range"), NumberRangeExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("list/add"), AddListExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("list/add_all"), AddAllListExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("list/get"), GetListExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("dict"), CreateDictExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("dict/put"), DictPutExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("dict/get"), DictGetExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("vec"), Vec3Expression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vector/add"), Vec3AddExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vector/multiply"), Vec3MultiplyExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vector/x"), Vec3XExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vector/y"), Vec3YExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("vector/z"), Vec3ZExpression.class);

        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/position"), EntityPositionExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/direction"), EntityDirectionExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/teleport"), EntityTeleportAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/teleport_relative"), EntityTeleportRelativeAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/world"), EntityWorldExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/set_name"), EntitySetNameAction.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/name"), EntityNameExpression.class);
        Registry.register(actions, ResourceLocation.withDefaultNamespace("entity/health"), EntityHealthExpression.class);
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
