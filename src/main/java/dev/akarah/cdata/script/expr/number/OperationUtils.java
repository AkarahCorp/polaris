package dev.akarah.cdata.script.expr.number;

import net.minecraft.network.chat.Component;

public class OperationUtils {
    public static Object add(Object lhs, Object rhs) {
        if(lhs instanceof Double ld && rhs instanceof Double rd) {
            return ld + rd;
        }
        if(lhs instanceof String ls && rhs instanceof Object rs) {
            return ls + rs;
        }
        if(lhs instanceof Component lc && rhs instanceof Component rc) {
            return Component.empty()
                    .append(lc)
                    .append(rc);
        }

        if(lhs instanceof Component lc && rhs instanceof Object rc) {
            return Component.empty()
                    .append(lc)
                    .append(rc.toString());
        }
        throw new RuntimeException("Can not add " + lhs + " and " + rhs);
    }
}
