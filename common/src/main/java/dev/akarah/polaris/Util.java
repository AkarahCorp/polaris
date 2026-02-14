package dev.akarah.polaris;

import java.util.concurrent.Callable;

public class Util {
    public static <T> T sneakyThrows(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
