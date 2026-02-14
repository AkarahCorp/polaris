package dev.akarah.polaris.building.wand;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WandTasks {
    private static final ConcurrentLinkedDeque<@NotNull Runnable> TASKS = new ConcurrentLinkedDeque<>();

    public static void pushTask(Runnable task) {
        TASKS.add(task);
    }

    public static void executeTasks() {
        var endAt = Instant.now().plusMillis(10);
        while(!TASKS.isEmpty()) {
            if(Instant.now().isAfter(endAt)) {
                return;
            }
            TASKS.removeFirst().run();
        }
    }
}
