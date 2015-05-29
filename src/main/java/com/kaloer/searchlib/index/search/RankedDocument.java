package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.Document;

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

    public T getDocument() {
        return document;
    }

    public int compareTo(RankedDocument o) {
        return Double.compare(o.score, score);
    }
}
