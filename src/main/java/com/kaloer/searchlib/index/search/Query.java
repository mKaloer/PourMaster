package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.Document;
import com.kaloer.searchlib.index.InvertedIndex;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by mkaloer on 07/05/15.
 */
public abstract class Query {

    private double boost = 1.0;

    public abstract Iterator<RankedDocument<Document>> search(InvertedIndex index) throws IOException;

    public void setBoost(double boost) {
        this.boost = boost;
    }

    public double getBoost() {
        return boost;
    }
}
