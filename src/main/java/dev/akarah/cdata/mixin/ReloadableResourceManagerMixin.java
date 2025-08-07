package dev.akarah.cdata.mixin;

import dev.akarah.cdata.Main;
import dev.akarah.cdata.db.persistence.DbPersistence;
import dev.akarah.cdata.registry.Resources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;

@Mixin(MultiPackResourceManager.class)
public abstract class ReloadableResourceManagerMixin implements ResourceManager {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void reloadLocalResources(PackType packType, List<?> list, CallbackInfo ci) {
        if(packType.equals(PackType.SERVER_DATA)) {
            try {
                if(Main.server() != null) {
                    System.out.println("Saving persistent data to file system...");
                    var start = Instant.now().toEpochMilli();
                    DbPersistence.savePersistentDb(Main.SERVER.registryAccess()).get();
                    var end = Instant.now().toEpochMilli();
                    System.out.println("All done! Finished in " + (end - start) + "ms");
                }

                Resources.reloadEverything(this);
            } catch (Exception ignored) {

            }
        }
    }
}
