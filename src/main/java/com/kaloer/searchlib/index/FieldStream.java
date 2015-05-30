package com.kaloer.searchlib.index;

import java.util.Iterator;

/**
 * Created by mkaloer on 15/04/15.
 */
public class FieldStream implements Iterable<TokenStream> {

    private Document document;

    public FieldStream(Document doc){
        this.document = doc;
    }

    public Document getDocument() {
        return document;
    }

    public Iterator<TokenStream> iterator() {
        return new Iterator<TokenStream>() {
            private int currentIndex = 0;
            public boolean hasNext() {
                return document.getFields().size() > currentIndex;
            }

            public TokenStream next() {
                return new TokenStream(document.getFields().get(currentIndex++));
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove elements from iterator.");
            }
        };
    }
}
