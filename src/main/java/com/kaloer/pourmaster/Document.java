package com.kaloer.pourmaster;

import com.kaloer.pourmaster.fields.FieldData;

import java.util.List;

/**
 * Represents data about an indexed document.
 */
public class Document {

    private static final int MAX_NUM_FIELDS = 255;

    private List<FieldData> fields;
    private long documentId;
    private int documentType;

    public Document() {

    }

    public List<FieldData> getFields() {
        return fields;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setFields(List<FieldData> fields) {
        if (fields.size() > 255) {
            throw new IllegalArgumentException(String.format("Too many fields in document. Maximum number of fields: %d", MAX_NUM_FIELDS));
        }
        this.fields = fields;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public void setDocumentType(int documentType) {
        this.documentType = documentType;
    }

    public int getDocumentType() {
        return documentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return this.documentId == ((Document) o).documentId;
    }

    @Override
    public int hashCode() {
        int result = (int) (documentId ^ (documentId >>> 32));
        result = 31 * result + documentType;
        return result;
    }
}
