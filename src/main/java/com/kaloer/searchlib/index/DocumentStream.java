package com.kaloer.searchlib.index;

import java.util.Iterator;

/**
 * Created by mkaloer on 15/04/15.
 */
public abstract class DocumentStream implements Iterable<FieldStream> {

    protected abstract boolean hasNextDocument();

    protected abstract FieldStream nextDocument();

    public Iterator<FieldStream> iterator() {
        return new Iterator<FieldStream>() {
            public boolean hasNext() {
                return hasNextDocument();
            }

            public FieldStream next() {
                return nextDocument();
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove elements from iterator.");
            }
        };
    }
}
