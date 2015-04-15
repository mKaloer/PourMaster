package com.kaloer.searchlib.index;

import java.util.Iterator;

/**
 * Created by mkaloer on 15/04/15.
 */
public abstract class DocumentStream implements Iterable<TokenStream> {

    protected abstract boolean hasNextDocument();
    protected abstract TokenStream nextDocument();

    public Iterator<TokenStream> iterator() {
        return new Iterator<TokenStream>() {
            public boolean hasNext() {
                return hasNextDocument();
            }

            public TokenStream next() {
                return nextDocument();
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove elements from iterator.");
            }
        }
    }
}
