package com.core.util;

import java.util.Arrays;

/**
 * User: jgreco
 */
public class ArrayUtils {
    public static <T> T[] append(T[] array, T item) {
        // create a copy with 1 more element and set the last element
        T[] newArray = Arrays.copyOf(array, array.length + 1);
        newArray[newArray.length - 1] = item;
        return newArray;
    }

    public static <T> T[] remove(T[] array, T item) {
        // create a copy with one less element
        T[] copy = Arrays.copyOf(array, array.length - 1);
        int index = 0;
        // assign each element of the array unless it is the item we are removing
        for (int i=0; i<array.length; i++) {
            T ci = array[i];
            if (item != ci) {
                copy[index++] = ci;
            }
        }
        return copy;
    }
}
