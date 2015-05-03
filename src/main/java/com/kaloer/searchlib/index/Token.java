package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.terms.StringTermType;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.terms.TermType;

/**
 * Created by mkaloer on 15/04/15.
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
