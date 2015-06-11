package com.kaloer.searchlib.index.search;

/**
 * Created by mkaloer on 07/05/15.
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
