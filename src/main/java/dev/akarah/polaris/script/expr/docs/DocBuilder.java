package dev.akarah.polaris.script.expr.docs;

import com.google.common.collect.Lists;
import dev.akarah.polaris.script.type.Type;
import dev.akarah.polaris.script.value.GlobalNamespace;

import java.util.ArrayList;
import java.util.List;

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
        this.documents.add(Document.fromClass(GlobalNamespace.class, "GlobalNamespace"));
        return this;
    }

    public List<Document> build() {
        return new ArrayList<>(this.documents);
    }
}
