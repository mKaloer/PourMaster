package com.kaloer.searchlib.index;

import java.io.IOException;

/**
 * Created by mkaloer on 13/04/15.
 */
public abstract class DocumentIndex {

    private FieldDataStore fieldDataStore;

    public DocumentIndex(FieldDataStore fieldDataStore) {
        this.fieldDataStore = fieldDataStore;
    }

    public abstract Document getDocument(long docId) throws IOException;

    public FieldDataStore getFieldDataStore() {
        return fieldDataStore;
    }
}
