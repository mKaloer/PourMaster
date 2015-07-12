package com.kaloer.pourmaster.search;

/**
 * Query for searching in a single field.
 */
public abstract class FieldQuery extends Query {

    private String field;

    public FieldQuery() {
        super();
    }

    public FieldQuery(String field) {
        super();
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
