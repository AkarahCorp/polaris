package dev.akarah.cdata.script.value;

public class GlobalNamespace {
    public static RList range(Double min, Double max) {
        var list = RList.create();
        var minIsLower = min < max;

        double idx = minIsLower ? min : max;
        while(idx <= (minIsLower ? max : min)) {
            RList.add(list, idx);
            idx += 1;
        }
        return list;
    }
}
