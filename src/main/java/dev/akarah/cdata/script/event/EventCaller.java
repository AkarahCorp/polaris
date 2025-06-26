package dev.akarah.cdata.script.event;

import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.ExtRegistries;
import dev.akarah.cdata.script.env.ScriptContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class EventCaller {
    public static void callEvent(ResourceLocation eventName, ScriptContext ctx) {
        var registry = Main.server().registryAccess().lookupOrThrow(ExtRegistries.SCRIPT);
        var tag = registry.getTagOrEmpty(TagKey.create(ExtRegistries.SCRIPT, eventName));
        tag.forEach(holder -> holder.value().execute(ctx));
    }
}
