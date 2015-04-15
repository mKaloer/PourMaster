package com.kaloer.searchlib.index;

import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class Document {

    private List fields;
    private long docId;
    private long documentId;

    public Document() {

    }

    public List<Field> getFields() {
        return fields;
    }

    public long getDocumentId() {
        return docId;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }
}
