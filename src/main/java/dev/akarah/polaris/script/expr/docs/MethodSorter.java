package dev.akarah.polaris.script.expr.docs;

import java.util.Comparator;

public class MethodSorter implements Comparator<Document.MethodEntry> {
    @Override
    public int compare(Document.MethodEntry first, Document.MethodEntry second) {
        // Compare by DocumentationOrdering first
        int orderCompare = first.order().compareTo(second.order());
        if (orderCompare != 0) {
            return orderCompare;
        }
        // If same order, compare by method name
        return first.name().compareTo(second.name());
    }
}
