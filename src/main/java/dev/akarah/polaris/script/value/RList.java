package dev.akarah.polaris.script.value;

import com.google.common.collect.Lists;
import dev.akarah.polaris.script.expr.ast.func.MethodTypeHint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RList extends RuntimeValue {
    private final List<RuntimeValue> inner;

    public RList(List<RuntimeValue> inner) {
        this.inner = inner;
    }

    @MethodTypeHint(signature = "<T>() -> list[T]", documentation = "Creates a new empty list.")
    public static RList create() {
        return new RList(Lists.newArrayList());
    }

    public static RList of(List<RuntimeValue> values) {
        return new RList(values);
    }

    @MethodTypeHint(signature = "<T>(this: list[T], index: number) -> nullable[T]", documentation = "Gets a value from the list.")
    public static RNullable get(RList $this, RNumber index) {
        try {
            return RNullable.of($this.inner.get(index.javaValue().intValue()));
        } catch (Exception e) {
            return RNullable.empty();
        }
    }

    @MethodTypeHint(signature = "<T>(this: list[T], index: number, value: T) -> void", documentation = "Sets a value in the list.")
    public static void set(RList $this, RNumber index, RuntimeValue value) {
        $this.inner.set(index.javaValue().intValue(), value);
    }

    @MethodTypeHint(signature = "<T>(this: list[T], value: T) -> void", documentation = "Adds a new value to the list.")
    public static void add(RList $this, RuntimeValue object) {
        $this.inner.add(object);
    }

    @MethodTypeHint(signature = "<T>(this: list[T], values: list[T]) -> void", documentation = "Adds all contents from the second list, into the first.")
    public static void add_all(RList $this, RList list) {
        $this.inner.addAll(list.inner);
    }

    @MethodTypeHint(signature = "<T>(this: list[T], value: T) -> boolean", documentation = "Returns true if the list contains the provided value.")
    public static RBoolean contains(RList $this, RuntimeValue value) {
        return RBoolean.of($this.inner.contains(value));
    }

    @MethodTypeHint(signature = "<T, U>(this: list[T], mapper: function(T) -> U) -> list[U]", documentation = "Returns a new list with the result of the mapping function for each element of this list.")
    public static RList map(RList $this, RFunction function) {
        var newList = RList.create();
        for(var entry : $this.javaValue()) {
            try {
                RList.add(newList, (RuntimeValue) function.javaValue().invoke(entry));
            } catch (Throwable _) {

            }
        }
        return newList;
    }

    @MethodTypeHint(signature = "<T>(this: list[T], predicate: function(T) -> boolean) -> list[T]", documentation = "Returns a new list, without any elements of this list that don't match the predicate.")
    public static RList filter(RList $this, RFunction function) {
        var newList = RList.create();
        for(var entry : $this.javaValue()) {
            try {
                if(((RBoolean) function.javaValue().invoke(entry)).javaValue()) {
                    RList.add(newList, entry);
                }
            } catch (Throwable _) {
                
            }
        }
        return newList;
    }

    public static RList copyInternal(RList $this) {
        var list = RList.create();
        for(var element : $this.javaValue()) {
            RList.add(list, element);
        }
        return list;
    }

    @MethodTypeHint(signature = "<T>(this: list[T]) -> list[T]", documentation = "Creates a copy of the provided list with no duplicate entries.")
    public static RList dedup(RList $this) {
        var list = RList.create();
        for(var element : $this.javaValue()) {
            if(!RList.contains(list, element).javaValue()) {
                RList.add(list, element);
            }
        }
        return list;
    }

    @MethodTypeHint(signature = "<T>(this: list[T], sort_by: function(T) -> number) -> list[T]", documentation = "Creates a copy of the provided list with no duplicate entries.")
    public static RList sorted(RList $this, RFunction sortBy) {
        return RList.of(
                new ArrayList<>(
                        $this.javaValue()
                            .stream()
                            .sorted((a, b) -> {
                                try {
                                    var aa = ((RNumber) sortBy.javaValue().invoke(a));
                                    var bb = ((RNumber) sortBy.javaValue().invoke(b));
                                    return aa.compareTo(bb);
                                } catch (Throwable e) {
                                    return 0;
                                }
                            })
                            .toList()
                )
        );
    }

    @MethodTypeHint(signature = "<T>(this: list[T], value: T) -> void", documentation = "Removes instances of the provided value from the list.")
    public static void remove(RList $this, RuntimeValue value) {
        $this.javaValue().remove(value);
    }

    @MethodTypeHint(signature = "<T>(this: list[T]) -> number", documentation = "Returns the length of the list.")
    public static RNumber size(RList $this) {
        return RNumber.of($this.inner.size());
    }

    @Override
    public List<RuntimeValue> javaValue() {
        return this.inner;
    }

    @Override
    public RuntimeValue copy() {
        var list = RList.create();
        for(var element : this.javaValue()) {
            RList.add(list, element.copy());
        }
        return list;
    }
}
