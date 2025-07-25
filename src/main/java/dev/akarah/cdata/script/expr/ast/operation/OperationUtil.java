package dev.akarah.cdata.script.expr.ast.operation;

import dev.akarah.cdata.script.value.RBoolean;
import dev.akarah.cdata.script.value.RNumber;
import dev.akarah.cdata.script.value.RString;
import dev.akarah.cdata.script.value.RText;
import dev.akarah.cdata.script.value.RuntimeValue;
import net.minecraft.network.chat.Component;

public class OperationUtil {
    public static RuntimeValue add(RuntimeValue lhs, RuntimeValue rhs) {
        if(rhs == null) {
            rhs = RString.of("null");
        }
        if(lhs instanceof RNumber ld && rhs instanceof RNumber rd) {
            return RNumber.of(ld.javaValue() + rd.javaValue());
        }
        if(lhs instanceof RString ls) {
            return RString.of(ls.javaValue() + rhs);
        }
        if(lhs instanceof RText lc && rhs instanceof RText rc) {
            return RText.of(Component.empty()
                    .append(lc.javaValue())
                    .append(rc.javaValue()));
        }
        if(lhs instanceof RText lc) {
            return RText.of(Component.empty()
                    .append(lc.javaValue())
                    .append(rhs.toString()));
        }
        throw new RuntimeException("Can not add " + lhs + " and " + rhs);
    }

    public static RuntimeValue and(RuntimeValue lhs, RuntimeValue rhs) {
        return RBoolean.of(((RBoolean) lhs).javaValue() && ((RBoolean) rhs).javaValue());
    }

    public static RuntimeValue or(RuntimeValue lhs, RuntimeValue rhs) {
        return RBoolean.of(((RBoolean) lhs).javaValue() || ((RBoolean) rhs).javaValue());
    }
}
