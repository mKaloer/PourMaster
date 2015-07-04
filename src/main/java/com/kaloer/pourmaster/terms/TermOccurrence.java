package com.kaloer.pourmaster.terms;

/**
 * Represents a single occurrence of a term in a field of a document.
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
