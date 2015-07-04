package com.kaloer.pourmaster.search;

import com.kaloer.pourmaster.Document;
import com.kaloer.pourmaster.InvertedIndex;

import java.io.IOException;
import java.util.Iterator;

/**
 * Base class for implementing a search query.
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
