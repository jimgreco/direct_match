package com.core.util.list;

/**
 * User: jgreco
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class IntrusiveDoublyLinkedList<T extends IntrusiveDoublyLinkedListItem> {
    protected T head;

	public int insert(T item) {
        int qspot = 0;
        T next = head;
        T prev = null;

        // walk the list
        while (next != null) {
            int compare = next.compare(item);

            if (compare > 0) {
                if (prev != null) {
                    prev.setNext(item);
                }
                else {
                    head = item;
                }

                next.setPrev(item);
                item.setPrev(prev);
                item.setNext(next);
                return qspot;
            }

            prev = next;
            next = (T)next.next();
            qspot++;
        }

        // insert at the end of the list
        if (prev != null) {
            prev.setNext(item);
        }
        else if (head == next) {
            head = item;  // are we actually at the HEAD?  make this the new HEAD
        }

        item.setPrev(prev);
        item.setNext(next);
        return qspot;
    }

    public void remove(T item) {
        IntrusiveDoublyLinkedListItem prev = item.prev();
        IntrusiveDoublyLinkedListItem next = item.next();

        if (prev != null) {
            prev.setNext(next);
        }
        else if (head == item) {
            head = (T) item.next();
        }

        if (next != null) {
            next.setPrev(prev);
        }
    }

    public int getQueueSpot(T item, int maxQueueSpot) {
        int qspot = 0;

        T next = head;
        while (next != null) {
            if (qspot >= maxQueueSpot) {
                qspot = maxQueueSpot;
                break;
            }

            if (item == next) {
                break;
            }
            next = (T)next.next();
            qspot++;
        }

        return qspot;
    }

    public T poll() {
        if (head == null) {
            return null;
        }

        T t = head;
        remove(head);
        return t;
    }

    public T peek() {
        return head;
    }
}
