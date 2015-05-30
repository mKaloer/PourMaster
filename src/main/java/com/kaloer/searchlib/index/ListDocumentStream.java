package com.kaloer.searchlib.index;

import java.util.ArrayList;

/**
 * Created by mkaloer on 15/04/15.
 */
public class ListDocumentStream extends DocumentStream {

    private ArrayList<Document> documents;
    private int docIndex = 0;

    public ListDocumentStream(ArrayList<Document> documents) {
        this.documents = documents;
    }

    public boolean hasNextDocument() {
        return documents.size() >= docIndex;
    }

    public FieldStream nextDocument() {
        return new FieldStream(documents.get(docIndex));
    }
}
