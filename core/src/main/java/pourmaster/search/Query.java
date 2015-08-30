package pourmaster.search;

import pourmaster.InvertedIndex;

import java.io.IOException;
import java.util.Iterator;

/**
 * Base class for implementing a search query.
 */
public abstract class Query {

    private double boost = 1.0;

    public abstract Iterator<RankedDocumentId> search(InvertedIndex index) throws IOException;

    public void setBoost(double boost) {
        this.boost = boost;
    }

    public double getBoost() {
        return boost;
    }
}
