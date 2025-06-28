package dev.akarah.cdata.registry.text;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class NullList<T> extends ArrayList<T> {
    public NullList(List<T> list) {
        super(list);
    }

    @Override
    public T get(int index) {
        try {
            return super.get(index);
        } catch (NoSuchElementException exception) {
            return null;
        }
    }

    @Override
    public T getFirst() {
        try {
            return super.getFirst();
        } catch (NoSuchElementException exception) {
            return null;
        }
    }
}
