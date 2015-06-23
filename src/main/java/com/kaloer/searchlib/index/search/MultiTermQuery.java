package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.Document;
import com.kaloer.searchlib.index.InvertedIndex;
import com.kaloer.searchlib.index.TermDictionary;
import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.fields.FieldData;
import com.kaloer.searchlib.index.postings.PostingsData;
import com.kaloer.searchlib.index.terms.TermOccurrence;
import com.kaloer.searchlib.index.util.IOIterator;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Used to combine multiple {@link TermQuery} instances into one query.
 */
public class MultiTermQuery extends Query {

    private ArrayList<TermQuery> subQueries = new ArrayList<TermQuery>();

    public void add(TermQuery query) {
        subQueries.add(query);
    }

    @Override
    public Iterator<RankedDocument<Document>> search(InvertedIndex index) throws IOException {
        // For each docId, accumulate scores per field (identified by id).
        HashMap<Long, HashMap<Integer, Double>> scores = new HashMap<Long, HashMap<Integer, Double>>();

        for (TermQuery query : subQueries) {
            Field queryField = index.getDocIndex().getFieldDataStore().getField(query.getFieldName());
            TermDictionary.TermData termData = index.getDictionary().findTerm(query.getTerm());
            if (termData == null) {
                // No results
                continue;
            }
            final IOIterator<PostingsData> docs = index.getPostings().getDocumentsForTerm(termData.getPostingsIndex(), termData.getDocFrequency());
            while (docs.hasNext()) {
                PostingsData postingsData = docs.next();
                int tf = 0;
                for (TermOccurrence occurrence : postingsData.getPositions()) {
                    Field field = index.getDocIndex().getFieldDataStore().getField(occurrence.getFieldId());
                    if (field.getFieldId() == queryField.getFieldId()) {
                        tf++;
                    }
                }
                // Only add if exists in searched field.
                if (tf > 0) {
                    if (!scores.containsKey(postingsData.getDocumentId())) {
                        scores.put(postingsData.getDocumentId(), new HashMap<Integer, Double>());
                    }
                    if (!scores.get(postingsData.getDocumentId()).containsKey(queryField.getFieldId())) {
                        scores.get(postingsData.getDocumentId()).put(queryField.getFieldId(), 0.0);
                    }
                    HashMap<Integer, Double> docScores = scores.get(postingsData.getDocumentId());
                    double prevScore = docScores.get(queryField.getFieldId());
                    double idf = Math.log((double) index.getDocIndex().getDocumentCount() / (double) (1 + termData.getFieldDocFrequency(queryField.getFieldId())));
                    double boostedScore = tf * idf * query.getBoost();
                    docScores.put(queryField.getFieldId(), prevScore + boostedScore);
                }
            }
        }

        final PriorityQueue<RankedDocument<Document>> result = new PriorityQueue<RankedDocument<Document>>();
        // Normalize scores and add to result set
        for (Long docId : scores.keySet()) {
            Document doc = index.getDocIndex().getDocument(docId);
            RankedDocument<Document> rankedDoc = new RankedDocument<Document>(doc, 0.0);
            for (int fieldId : scores.get(docId).keySet()) {
                for (FieldData f : doc.getFields()) {
                    if (f.getField().getFieldId() == fieldId) {
                        double normalizedScore = scores.get(docId).get(fieldId) / (double) f.getLength();
                        rankedDoc.setScore(rankedDoc.getScore() + normalizedScore);
                        break;
                    }
                }
            }
            result.add(rankedDoc);
        }

        // Return iterator
        return new Iterator<RankedDocument<Document>>() {
            public boolean hasNext() {
                return result.size() > 0;
            }

            public RankedDocument next() {
                return result.poll();
            }

            public void remove() {
                throw new NotImplementedException();
            }
        };
    }
}
