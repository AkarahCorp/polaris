package dev.akarah.polaris;

import dev.akarah.polaris.script.expr.docs.DocBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenerateDocs {
    public static void main(String[] args) {
        var docs = DocBuilder.builder().addAllTypes().build();

        var dir = Paths.get("./run/docs");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (var doc : docs) {
            var filename = doc.className() + ".md";
            var path = dir.resolve(filename);

            try {
                Files.writeString(path, doc.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}