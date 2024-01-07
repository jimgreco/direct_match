package com.core.util.list;

/**
 * User: jgreco
 */
@SuppressWarnings({"rawtypes"})
public interface IntrusiveDoublyLinkedListItem<T extends IntrusiveDoublyLinkedListItem> {
    T next();
    T prev();
    void setNext(T next);
    void setPrev(T prev);
    int compare(T item);
}
