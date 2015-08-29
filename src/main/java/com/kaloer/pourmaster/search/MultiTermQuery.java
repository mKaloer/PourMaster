package com.kaloer.pourmaster.search;

import com.kaloer.pourmaster.InvertedIndex;

import java.io.IOException;
import java.util.*;

/**
 * Used to combine multiple {@link FieldQuery} instances into one query.
 */
public class MultiTermQuery extends Query {

    private final ArrayList<FieldQuery> subQueries = new ArrayList<FieldQuery>();

    public void add(FieldQuery query) {
        subQueries.add(query);
    }

    @Override
    public Iterator<RankedDocumentId> search(InvertedIndex index) throws IOException {
        // For each docId, accumulate scores
        final HashMap<Long, Double> scores = new HashMap<Long, Double>();

        for (FieldQuery query : subQueries) {
            Iterator<RankedDocumentId> subResult = query.search(index);
            while (subResult.hasNext()) {
                // Add scores per document
                RankedDocumentId doc = subResult.next();
                Double docScore = scores.remove(doc.getDocument());
                if (docScore == null) {
                    docScore = 0.0;
                }
                scores.put(doc.getDocument(), docScore + doc.getScore());
            }
        }

        final ArrayList<Map.Entry<Long, Double>> scoreEntries = new ArrayList<Map.Entry<Long, Double>>(scores.entrySet());
        Collections.sort(scoreEntries, new Comparator<Map.Entry<Long, Double>>() {
            public int compare(Map.Entry<Long, Double> o1, Map.Entry<Long, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        return new Iterator<RankedDocumentId>() {
            int index = 0;
            public boolean hasNext() {
                return index < scoreEntries.size();
            }

            public RankedDocumentId next() {
                Map.Entry<Long, Double> entry = scoreEntries.get(index++);
                return new RankedDocumentId(entry.getKey(), entry.getValue() * getBoost());
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
