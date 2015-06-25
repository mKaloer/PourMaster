package com.kaloer.pourmaster.search;

/**
 * Represents a document and its relevance score.
 */
public class RankedDocument<T extends Object> implements Comparable<RankedDocument> {

    private T document;
    private double score;

    public RankedDocument(T document, double score) {
        this.document = document;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public T getDocument() {
        return document;
    }

    public int compareTo(RankedDocument o) {
        return Double.compare(o.score, score);
    }
}
