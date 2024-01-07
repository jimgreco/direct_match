package com.core.util.datastructures;

/**
 * Created by johnlevidy on 6/2/15.
 */
public abstract class ClassFactory<T> {

    private final Class<T> clazz;

    public ClassFactory(Class<T> clazz){
        this.clazz = clazz;
    }

    public abstract T newInstance();

    public Class<T> getClazz() {
        return this.clazz;
    }
}
