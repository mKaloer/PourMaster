package com.kaloer.searchlib.index;

import java.util.Iterator;
import java.util.List;

/**
 * Created by mkaloer on 15/04/15.
 */
public class FieldStream implements Iterable<TokenStream> {

    private List<Field> fields;

    public FieldStream(List<Field> fields){
        this.fields = fields;
    }

    public Iterator<TokenStream> iterator() {
        return new Iterator<TokenStream>() {
            private int currentIndex = 0;
            public boolean hasNext() {
                return fields.size() > currentIndex;
            }

            public TokenStream next() {
                return new TokenStream(fields.get(currentIndex++));
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove elements from iterator.");
            }
        };
    }
}
