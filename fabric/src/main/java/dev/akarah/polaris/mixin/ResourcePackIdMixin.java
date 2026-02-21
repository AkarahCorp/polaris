package dev.akarah.polaris.mixin;

import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.UUID;

@Mixin(DedicatedServerProperties.class)
public class ResourcePackIdMixin {
    @ModifyVariable(
            method = "getServerPackInfo",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            index = 0
    )
    private static String mrow(String value) {
        return UUID.randomUUID().toString();
    }
}
