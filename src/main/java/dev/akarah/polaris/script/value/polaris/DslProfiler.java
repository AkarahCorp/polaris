package dev.akarah.polaris.script.value.polaris;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.akarah.polaris.Main;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DslProfiler {
    public static Map<UUID, DslProfileFilter> profileSourceFilters = new HashMap<>();

    public record DslProfileFilter(
            String name,
            double durationInMicros
    ) {
    }

    String sourceEvent;
    Map<String, Duration> times = Maps.newHashMap();
    List<MiniFrame> stackFrames = Lists.newArrayList();

    record MiniFrame(String name, Instant start) {}

    private DslProfiler(String sourceEvent) {
        this.sourceEvent = sourceEvent;
    }

    public static DslProfiler create(String sourceEvent) {
        return new DslProfiler(sourceEvent);
    }

    public void enter(String string) {
        stackFrames.add(new MiniFrame(string, Instant.now()));
    }

    public void exit() {
        var last = stackFrames.removeLast();
        if(!stackFrames.contains(last)) {
            var duration = Duration.between(last.start, Instant.now());
            this.times.compute(last.name(), (k, v) -> v == null ? duration : v.plus(duration));
        }
        if(stackFrames.isEmpty()) {
            for(var player : Main.server().getPlayerList().getPlayers()) {
                if(profileSourceFilters.containsKey(player.getUUID())) {
                    var filter = profileSourceFilters.get(player.getUUID());
                    if(this.sourceEvent.contains(filter.name()) && this.totalTime().toNanos() / 1000.0 > filter.durationInMicros) {
                        player.displayClientMessage(this.postSample(), false);
                    }
                }
            }
        }
    }

    public Component postSample() {
        var base = Component.literal(" DSL Profiler Results (" + this.sourceEvent + "):");
        var entries = this.times.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).limit(5).toList();
        for(var entry : entries) {
            base.append("\n  " + entry.getKey() + ": " + (entry.getValue().toNanos() / 1000.0) + "µs");
        }
        if(this.times.size() > 5) {
            base.append("\n ... and " + (this.times.size() - 5) + " more");
        }
        base.append("Total time: " + this.totalTime());
        return base;
    }

    public Duration totalTime() {
        return this.times.values().stream().reduce(Duration.ZERO, Duration::plus);
    }
}
