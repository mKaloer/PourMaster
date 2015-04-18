package com.kaloer.searchlib.index;

/**
 * Created by mkaloer on 15/04/15.
 */
public class Token {

    private Object value;
    private int position;

    public Token(Object value, int position) {
        this.value = value;
        this.position = position;
    }

    public Object getValue() {
        return value;
    }

    public int getPosition() {
        return position;
    }
}
