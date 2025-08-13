package dev.akarah.cdata.db.persistence;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdCompressCtx;
import com.mojang.datafixers.util.Pair;
import dev.akarah.cdata.db.DataStore;
import dev.akarah.cdata.db.Database;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.time.Instant;
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
            var path = Paths.get("./saved/" + name + ".db.zst");
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

                Files.write(path, zip(arr));
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
                files.forEach(file -> {
                    if(file.toString().endsWith(".db")) {
                        try {
                            var contents = Files.readAllBytes(file);
                            Files.deleteIfExists(file);
                            Files.write(Paths.get(file.toString().replace(".db", ".db.zst")), zip(contents));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            try(var files = Files.walk(rootDir)) {
                entries = files.filter(x -> x.toString().endsWith(".db.zst")).toList();
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
                var start = Instant.now().toEpochMilli();
                var rawBytes = Files.readAllBytes(path);
                var readTime = Instant.now().toEpochMilli();
                var file = unzip(rawBytes);
                var unzipTime = Instant.now().toEpochMilli();
                var bb = ByteBuffer.wrap(file);
                var buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bb), access);
                var bufferingTime = Instant.now().toEpochMilli();
                var decoded = DbCodecs.TREE_MAP_CODEC.decode(buf);
                var decodingTime = Instant.now().toEpochMilli();
                var ds = DataStore.of(decoded);
                var output = Pair.of(path.toString().replace("./saved/", "").replace(".db.zst", ""), ds);
                var end = Instant.now().toEpochMilli();
                synchronized (System.out) {
                    System.out.println(path.toString().replace("./saved/", "") + " took " + (end - start) + "ms");
                    System.out.println("-> Reading took: " + (readTime - start) + "ms");
                    System.out.println("-> Decompression took: " + (unzipTime - readTime) + "ms");
                    System.out.println("-> Putting into a buffer took: " + (bufferingTime - unzipTime) + "ms");
                    System.out.println("-> Decoding took: " + (decodingTime - bufferingTime) + "ms");
                }
                return output;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static byte[] zip(byte[] input) {
        byte[] output;
        try(var ctx = new ZstdCompressCtx()) {
            ctx.setLevel(18);
            output = ctx.compress(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    public static byte[] unzip(byte[] input) {
        return Zstd.decompress(input);
    }
}
