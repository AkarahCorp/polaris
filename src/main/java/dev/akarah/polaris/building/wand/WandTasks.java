package dev.akarah.polaris.building.wand;

import net.minecraft.util.ArrayListDeque;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
