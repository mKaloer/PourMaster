package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.*;
import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.terms.TermOccurrence;

import java.io.IOException;
import java.util.*;

/**
 * Created by mkaloer on 07/05/15.
 */
public class MultiTermQuery extends Query {

    private ArrayList<TermQuery> subQueries = new ArrayList<TermQuery>();

    public void add(TermQuery query) {
        subQueries.add(query);
    }

    @Override
    public Iterator<RankedDocument> search(InvertedIndex index) throws IOException {
        HashMap<Long, Double> scores = new HashMap<Long, Double>();

        for(TermQuery query : subQueries) {
            TermDictionary.TermData termData = index.getDictionary().findTerm(query.getTerm());
            if(termData == null) {
                // No results
                return Collections.emptyIterator();
            }
            final Iterator<PostingsData> docs = index.getPostings().getDocumentsForTerm(termData.getPostingsIndex(), termData.getDocFrequency());
            while(docs.hasNext()) {
                PostingsData postingsData = docs.next();
                int tf = 0;
                for(TermOccurrence occurrence : postingsData.getPositions()) {
                    if(occurrence.getFieldId() == query.getFieldId()) {
                        tf++;
                    }
                }
                if(!scores.containsKey(postingsData.getDocumentId())) {
                    scores.put(postingsData.getDocumentId(), 0.0);
                }
                double prevScore = scores.get(postingsData.getDocumentId());
                double idf = Math.log((double) index.getDocIndex().getDocumentCount() / (double) (1 + termData.getDocFrequency()));
                scores.put(postingsData.getDocumentId(), prevScore + tf * idf);
            }
        }

        PriorityQueue<RankedDocument> result = new PriorityQueue<RankedDocument>();
        // Normalize scores and add to result set
        for(Long docId : scores.keySet()) {
            Document doc = index.getDocIndex().getDocument(docId);
            for(Field f : doc.getFields()) {
                for(TermQuery query : subQueries) {
                    if(f.getFieldId() == query.getFieldId()) {
                        result.add(new RankedDocument(doc, scores.get(docId) / (double) f.getLength()));
                        break;
                    }
                }
            }
        }

        return result.iterator();
    }
}
