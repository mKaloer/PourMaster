package com.kaloer.pourmaster.util;

import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Iterator for a {@link PriorityQueue} which will remove its elements whenever {@code next()} is called.
 */
public class PriorityQueueIterator<E> implements Iterator<E> {

    private final PriorityQueue<E> queue;

    public PriorityQueueIterator(PriorityQueue<E> queue) {
        this.queue = queue;
    }

    public boolean hasNext() {
        return queue.size() > 0;
    }

    public E next() {
        return queue.poll();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
