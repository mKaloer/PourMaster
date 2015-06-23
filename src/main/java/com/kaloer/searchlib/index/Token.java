package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.terms.Term;

/**
 * Represents an occurrence of a term in a field of a document.
 */
public class Token<T extends Term> {

    private T value;
    private int position;

    public Token(T value, int position) {
        this.value = value;
        this.position = position;
    }

    public T getValue() {
        return value;
    }

    public int getPosition() {
        return position;
    }
}
