package dev.akarah.polaris.script.expr.ast.operation;

import dev.akarah.polaris.script.value.RBoolean;
import dev.akarah.polaris.script.value.RNumber;
import dev.akarah.polaris.script.value.RString;
import dev.akarah.polaris.script.value.RText;
import dev.akarah.polaris.script.value.RuntimeValue;
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
