package dev.akarah.cdata.script.value;

import com.google.common.collect.Lists;
import dev.akarah.cdata.script.expr.ast.func.MethodTypeHint;

import java.util.ArrayList;
import java.util.List;

public class RList extends RuntimeValue {
    private final List<RuntimeValue> inner;

    public RList(List<RuntimeValue> inner) {
        this.inner = inner;
    }

    @MethodTypeHint("<T>() -> list[T]")
    public static RList create() {
        return new RList(Lists.newArrayList());
    }

    @MethodTypeHint("<T>(this: list[T], index: number) -> nullable[T]")
    public static RNullable get(RList $this, RNumber index) {
        try {
            return RNullable.of($this.inner.get(index.javaValue().intValue()));
        } catch (Exception e) {
            return RNullable.empty();
        }
    }

    @MethodTypeHint("<T>(this: list[T], value: T) -> void")
    public static void add(RList $this, RuntimeValue object) {
        $this.inner.add(object);
    }

    @MethodTypeHint("<T>(this: list[T], values: list[T]) -> void")
    public static void add_all(RList $this, RList list) {
        $this.inner.addAll(list.inner);
    }

    @MethodTypeHint("<T>(this: list[T], value: T) -> boolean")
    public static RBoolean contains(RList $this, RuntimeValue value) {
        return RBoolean.of($this.inner.contains(value));
    }

    @MethodTypeHint("<T>(this: list[T], mapper: function(T) -> T) -> list[T]")
    public static RList map(RList $this, RFunction function) {
        var newList = RList.create();
        for(var entry : $this.javaValue()) {
            try {
                RList.add(newList, (RuntimeValue) function.javaValue().invoke(entry));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return newList;
    }

    @MethodTypeHint("<T>(this: list[T], predicate: function(T) -> boolean) -> list[T]")
    public static RList filter(RList $this, RFunction function) {
        var newList = RList.create();
        for(var entry : $this.javaValue()) {
            try {
                if(((RBoolean) function.javaValue().invoke(entry)).javaValue()) {
                    RList.add(newList, entry);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return newList;
    }

    @Override
    public List<RuntimeValue> javaValue() {
        return this.inner;
    }
}
