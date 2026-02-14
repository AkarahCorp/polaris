package dev.akarah.polaris;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class Scheduler {
    Map<Integer, List<Runnable>> tasks = Maps.newHashMap();

    public void schedule(int delay, Runnable task) {
        try {
            var occursAt = Main.server().getTickCount() + delay;
            if(!tasks.containsKey(occursAt)) {
                tasks.put(occursAt, Lists.newArrayList());
            }

            var list = tasks.get(occursAt);
            list.add(task);
        } catch (Exception e) {
            if(!tasks.containsKey(delay)) {
                tasks.put(delay, Lists.newArrayList());
            }
            var list = tasks.get(delay);
            list.add(task);
        }
    }

    public void tick() {
        if(this.tasks.containsKey(Main.server().getTickCount())) {
            for(var task : this.tasks.remove(Main.server().getTickCount())) {
                task.run();
            }
        }
    }
}