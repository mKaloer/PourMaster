package com.kaloer.searchlib.index.terms;

/**
 * Created by mkaloer on 08/05/15.
 */
public class TermOccurrence {
    private long position;
    private int fieldId;

    public TermOccurrence(long position, int fieldId) {
        this.position = position;
        this.fieldId = fieldId;
    }

    public int getFieldId() {
        return fieldId;
    }

    public long getPosition() {
        return position;
    }
}
