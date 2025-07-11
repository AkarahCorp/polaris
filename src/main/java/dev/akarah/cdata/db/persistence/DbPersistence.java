package dev.akarah.cdata.db.persistence;

import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.Main;
import dev.akarah.cdata.db.DataStore;
import dev.akarah.cdata.db.Database;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.io.FileExistsException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DbPersistence {
    public static CompletableFuture<Void> savePersistentDb(RegistryAccess access) {
        var entries = Database.save().dataStores().toList();
        var futures = new CompletableFuture[entries.size()];
        for(int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            futures[i] = saveDataStore(entry.getKey(), entry.getValue(), access);
        }
        return CompletableFuture.allOf(futures);
    }

    public static CompletableFuture<Void> saveDataStore(String name, DataStore dataStore, RegistryAccess access) {
        return CompletableFuture.runAsync(() -> {
            var path = Paths.get("./saved/" + name + ".db");
            var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), access);
            DbCodecs.TREE_MAP_CODEC.encode(buf, dataStore.map());
            try {
                try {
                    Files.createDirectories(path);
                } catch (FileAlreadyExistsException ignored) {

                }
                try {
                    Files.delete(path);
                } catch (NoSuchFileException ignored) {

                }
                try {
                    Files.createFile(path);
                } catch (FileAlreadyExistsException ignored) {

                }
                var idx = buf.writerIndex();
                var arr = new byte[idx];
                System.arraycopy(buf.array(), 0, arr, 0, arr.length);
                Files.write(path, arr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static CompletableFuture<Void> loadPersistentDb(RegistryAccess access) {
        try {
            List<Path> entries;

            var rootDir = Paths.get("./saved/");
            Files.createDirectories(rootDir);

            try(var files = Files.walk(rootDir)) {
                entries = files.filter(x -> x.toString().endsWith(".db")).toList();
            }

            var futures = new CompletableFuture[entries.size()];
            for(int i = 0; i < entries.size(); i++) {
                var entry = entries.get(i);
                futures[i] = loadDataStore(entry, access);
            }

            return CompletableFuture.allOf(futures)
                    .thenApply(_ -> Arrays.stream(futures).map(CompletableFuture::join).map(x -> (Pair<String, DataStore>) x).toList())
                    .thenAccept(list -> list.forEach(ds -> Database.save().writeDataStore(ds.getFirst(), ds.getSecond())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static CompletableFuture<Pair<String, DataStore>> loadDataStore(Path path, RegistryAccess access) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var file = Files.readAllBytes(path);
                var bb = ByteBuffer.wrap(file);
                var buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bb), access);
                var decoded = DbCodecs.TREE_MAP_CODEC.decode(buf);
                var ds = DataStore.of(decoded);
                return Pair.of(path.toString().replace("./saved/", "").replace(".db", ""), ds);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
