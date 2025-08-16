package dev.akarah.polaris.script.expr.docs;

import com.google.common.collect.Lists;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.GlobalNamespace;

import java.util.List;
import java.util.stream.Collectors;

public class DocBuilder {
    List<Document> documents = Lists.newArrayList();

    private DocBuilder() {}

    public static DocBuilder builder() {
        return new DocBuilder();
    }

    public DocBuilder addAllTypes() {
        this.documents.addAll(
                Type.allTypes()
                        .stream()
                        .map(Document::fromType)
                        .toList()
        );
        this.documents.add(Document.fromClass(GlobalNamespace.class, "Global Namespace"));
        return this;
    }

    public String build() {
        return this.documents
                .stream()
                .map(Document::toString)
                .map(x -> x + "\n\n\n")
                .collect(Collectors.joining());
    }
}
