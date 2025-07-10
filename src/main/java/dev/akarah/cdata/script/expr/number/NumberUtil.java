package dev.akarah.cdata.script.expr.number;

import dev.akarah.cdata.script.expr.Expression;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;

public class NumberUtil {
    public static void bootStrap(Registry<Class<? extends Expression>> actions) {

    }

    public static Object add(Object lhs, Object rhs) {
        if(rhs == null) {
            rhs = "null";
        }
        if(lhs instanceof Double ld && rhs instanceof Double rd) {
            return ld + rd;
        }
        if(lhs instanceof String ls) {
            return ls + rhs;
        }
        if(lhs instanceof Component lc && rhs instanceof Component rc) {
            return Component.empty()
                    .append(lc)
                    .append(rc);
        }
        if(lhs instanceof Component lc) {
            return Component.empty()
                    .append(lc)
                    .append(rhs.toString());
        }
        throw new RuntimeException("Can not add " + lhs + " and " + rhs);
    }
}
