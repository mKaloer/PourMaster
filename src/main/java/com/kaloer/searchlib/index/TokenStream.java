package com.kaloer.searchlib.index;

import java.util.Iterator;

/**
 * Created by mkaloer on 15/04/15.
 */
public class TokenStream implements Iterable<Token> {

    public TokenStream(Document doc) {
        for(Field f : doc.getFields()) {
            if(f.getIsIndexed()) {
                // Should be indexed
            }
        }
    }

    protected boolean hasNextToken() {
        return false;
    }

    protected Token nextToken() {
        return null;
    }

    public Iterator<Token> iterator() {
        return new Iterator<Token>() {
            public boolean hasNext() {
                return hasNextToken();
            }

            public Token next() {
                return nextToken();
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove elements from iterator.");
            }
        }
    }
}
