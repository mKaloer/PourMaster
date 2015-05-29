package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.Document;

/**
 * Created by mkaloer on 07/05/15.
 */
public class RankedDocument implements Comparable<RankedDocument> {

    private Document document;
    private double score;

    public RankedDocument(Document document, double score) {
        this.document = document;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public Document getDocument() {
        return document;
    }

    public int compareTo(RankedDocument o) {
        return Double.compare(o.score, score);
    }
}
