package dev.akarah.cdata;

import java.util.Optional;

public class TestClass {
    public static void main() {
        var n = Optional.of(10);
        var y = 2;
        n.map(x -> x * y);
    }
}
