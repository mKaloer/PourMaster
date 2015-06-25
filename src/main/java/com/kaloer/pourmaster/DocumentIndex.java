package com.kaloer.pourmaster;

import java.io.IOException;

/**
 * Index for mapping document ids to actual documents.
 */
public abstract class DocumentIndex {

    private FieldDataStore fieldDataStore;

    public abstract void init(IndexConfig config) throws IOException;

    protected void setFieldDataStore(FieldDataStore fieldDataStore) {
        this.fieldDataStore = fieldDataStore;
    }

    public abstract Document getDocument(long docId) throws IOException;

    public abstract void addDocument(Document doc) throws IOException, ReflectiveOperationException;

    public FieldDataStore getFieldDataStore() {
        return fieldDataStore;
    }

    public abstract long getDocumentCount() throws IOException;

    public abstract void deleteAll() throws IOException;
}
