package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.Document;
import com.kaloer.searchlib.index.InvertedIndex;
import com.kaloer.searchlib.index.postings.PostingsData;
import com.kaloer.searchlib.index.TermDictionary;
import com.kaloer.searchlib.index.fields.Field;
import com.kaloer.searchlib.index.fields.FieldData;
import com.kaloer.searchlib.index.terms.TermOccurrence;
import com.kaloer.searchlib.index.util.IOIterator;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    public Iterator<RankedDocument<Document>> search(InvertedIndex index) throws IOException {
        HashMap<Long, Double> scores = new HashMap<Long, Double>();

        for(TermQuery query : subQueries) {
            Field queryField = index.getDocIndex().getFieldDataStore().getField(query.getFieldName());
            TermDictionary.TermData termData = index.getDictionary().findTerm(query.getTerm());
            if(termData == null) {
                // No results
                return Collections.emptyIterator();
            }
            final IOIterator<PostingsData> docs = index.getPostings().getDocumentsForTerm(termData.getPostingsIndex(), termData.getDocFrequency());
            while(docs.hasNext()) {
                PostingsData postingsData = docs.next();
                int tf = 0;
                for(TermOccurrence occurrence : postingsData.getPositions()) {
                    Field field = index.getDocIndex().getFieldDataStore().getField(occurrence.getFieldId());
                    if(field.getFieldId() == queryField.getFieldId()) {
                        tf++;
                    }
                }
                if(!scores.containsKey(postingsData.getDocumentId())) {
                    scores.put(postingsData.getDocumentId(), 0.0);
                }
                double prevScore = scores.get(postingsData.getDocumentId());
                double idf = Math.log((double) index.getDocIndex().getDocumentCount() / (double) (1 + termData.getFieldDocFrequency(queryField.getFieldId())));
                scores.put(postingsData.getDocumentId(), prevScore + tf * idf);
            }
        }

        final PriorityQueue<RankedDocument<Document>> result = new PriorityQueue<RankedDocument<Document>>();
        // Normalize scores and add to result set
        for(Long docId : scores.keySet()) {
            Document doc = index.getDocIndex().getDocument(docId);
            for(FieldData f : doc.getFields()) {
                for(TermQuery query : subQueries) {
                    if(f.getField().getFieldName().equals(query.getFieldName())) {
                        result.add(new RankedDocument<Document>(doc, scores.get(docId) / (double) f.getLength()));
                        break;
                    }
                }
            }
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
