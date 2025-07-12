package dev.akarah.cdata.script.value;

import java.util.ArrayList;
import java.util.List;

public class RList {
    List<Object> internal = new ArrayList<>();

    public static RList create() {
        return new RList();
    }

    public static RList range(Double min, Double max) {
        var list = RList.create();
        var minIsLower = min < max;

        double idx = minIsLower ? min : max;
        while(idx <= (minIsLower ? max : min)) {
            list.add(idx);
            idx += 1;
        }
        return list;
    }

    public Object get(Double index) {
        return this.internal.get(index.intValue());
    }

    public void add(Object object) {
        this.internal.add(object);
    }

    public void add_all(RList list) {
        this.internal.addAll(list.internal);
    }
}
