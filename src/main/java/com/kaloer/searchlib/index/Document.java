package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.Field;

import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class Document {

    private static final int MAX_NUM_FIELDS = 255;

    private List fields;
    private long documentId;

    public Document() {

    }

    public List<Field> getFields() {
        return fields;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setFields(List<Field> fields) {
        if(fields.size() > 255) {
            throw new IllegalArgumentException(String.format("Too many fields in document. Maximum number of fields: %d", MAX_NUM_FIELDS));
        }
        this.fields = fields;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }
}
