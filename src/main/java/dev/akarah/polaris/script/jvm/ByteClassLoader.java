package dev.akarah.polaris.script.jvm;

import com.google.common.collect.Maps;

import java.util.Map;

public class ByteClassLoader extends ClassLoader {
    Map<String, byte[]> classes = Maps.newHashMap();

    public ByteClassLoader(ClassLoader contextClassLoader) {
        super(contextClassLoader);
    }

    public void registerClass(String descriptor, byte[] array) {
        this.classes.put(descriptor, array);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(this.classes.containsKey(name)) {
            var arr = this.classes.get(name);
            return this.defineClass(name, arr, 0, arr.length);
        } else {
            return super.findClass(name);
        }
    }
}
