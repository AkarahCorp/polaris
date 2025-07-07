package dev.akarah.cdata.mixin;

import com.mojang.datafixers.DataFixer;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.registry.ExtReloadableResources;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow private int tickCount;

    @Inject(at = @At("CTOR_HEAD"), method = "<init>")
    private void getInstanceAndStartWork(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
        Main.SERVER = (MinecraftServer) (Object) this;
    }

    @Inject(at = @At("HEAD"), method = "tickChildren")
    public void onTick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        ExtReloadableResources.statManager().loopPlayers();
        if(this.tickCount % 200 == 0) {
            ExtReloadableResources.statManager().refreshPlayerInventories();
        }
    }
}
