package com.kaloer.searchlib.index.search;

/**
 * Created by mkaloer on 06/05/15.
 */
public class DocumentScore {
    private double score;
    private long docId;

    public DocumentScore(double score, long docId) {
        this.score = score;
        this.docId = docId;
    }

    public double getScore() {
        return score;
    }

    public long getDocumentId() {
        return docId;
    }
}
