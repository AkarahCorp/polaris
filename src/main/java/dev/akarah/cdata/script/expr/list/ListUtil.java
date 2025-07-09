package dev.akarah.cdata.script.expr.list;

import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class ListUtil {
    public static List<Double> range(Double min, Double max) {
        var list = Lists.<Double>newArrayList();
        var minIsLower = min < max;

        double idx = minIsLower ? min : max;
        while(idx <= (minIsLower ? max : min)) {
            list.add(idx);
            idx += 1;
        }
        return list;
    }
}
