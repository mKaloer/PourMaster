package com.kaloer.pourmaster.search;

import com.kaloer.pourmaster.Document;
import com.kaloer.pourmaster.InvertedIndex;
import com.kaloer.pourmaster.TermDictionary;
import com.kaloer.pourmaster.fields.Field;
import com.kaloer.pourmaster.postings.PostingsData;
import com.kaloer.pourmaster.terms.Term;
import com.kaloer.pourmaster.terms.TermOccurrence;
import com.kaloer.pourmaster.util.IOIterator;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Query for a single term in a specific field. Typically used with a
 * {@link MultiTermQuery}.
 */
public class TermQuery extends FieldQuery {

    private Term term;

    public TermQuery(Term term, String fieldName) {
        super(fieldName);
        this.term = term;
    }

    @Override
    public Iterator<RankedDocumentId> search(InvertedIndex index) throws IOException {
        TermDictionary.TermData termData = index.getDictionary().findTerm(term);
        if (termData == null) {
            // No results
            return Collections.emptyIterator();
        }
        long documentCount = index.getDocIndex().getDocumentCount();
        final IOIterator<PostingsData> postingsData = index.getPostings().getDocumentsForTerm(termData.getPostingsIndex(), termData.getDocFrequency());
        Field queryField = index.getDocIndex().getFieldDataStore().getField(getField());
        int fieldId = queryField.getFieldId();
        PriorityQueue<RankedDocumentId> result = new PriorityQueue<RankedDocumentId>();
        // Normalize scores and add to result set
        while (postingsData.hasNext()) {
            PostingsData data = postingsData.next();
            long docId = data.getDocumentId();
            int tf = 0;
            for (TermOccurrence occurrence : data.getPositions()) {
                if (occurrence.getFieldId() == fieldId) {
                    tf++;
                }
            }
            if (tf > 0) {
                double idf = Math.log((double) documentCount / (double) (1 + termData.getFieldDocFrequency(queryField.getFieldId())));

                // Field length normalization
                float fieldNorm = index.getFieldNormsStore().getFieldNorm(fieldId, docId);
                result.add(new RankedDocumentId(docId, tf * idf * getBoost() * fieldNorm));
            }
        }
        return result.iterator();
    }

    @Override
    public double getBoost() {
        return super.getBoost();
    }

    public Term getTerm() {
        return term;
    }
}
