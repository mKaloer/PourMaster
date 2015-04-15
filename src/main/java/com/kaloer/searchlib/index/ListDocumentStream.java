package com.kaloer.searchlib.index;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by mkaloer on 15/04/15.
 */
public class ListDocumentStream extends DocumentStream {

    private ArrayList<Document> documents;

    public ListDocumentStream(ArrayList<Document> documents) {
        this.documents = documents;
    }

    public boolean hasNextDocument() {
        return false;
    }

    public TokenStream nextDocument() {
        return null;
    }

    public boolean hasNextToken() {
        return false;
    }

    public Token nextToken() {
        return null;
    }
}
