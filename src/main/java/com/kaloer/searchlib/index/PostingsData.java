package com.kaloer.searchlib.index;

/**
 * Created by mkaloer on 12/04/15.
 */
public class PostingsData {
    private long documentId;
    private long[] positions;

    public PostingsData(long docId, long[] positions) {
        this.documentId = docId;
        this.positions = positions;
    }

    public long[] getPositions() {
        return positions;
    }

    public long getDocumentId() {
        return documentId;
    }
}
