package com.kaloer.pourmaster.search;

import com.kaloer.pourmaster.Document;
import com.kaloer.pourmaster.InvertedIndex;
import com.kaloer.pourmaster.TermDictionary;
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
public class TermQuery extends Query {

    private Term term;
    private String fieldName;

    public TermQuery(Term term, String fieldName) {
        this.term = term;
        this.fieldName = fieldName;
    }

    @Override
    public Iterator<RankedDocument<Document>> search(InvertedIndex index) throws IOException {
        TermDictionary.TermData termData = index.getDictionary().findTerm(term);
        if (termData == null) {
            // No results
            return Collections.emptyIterator();
        }
        final IOIterator<PostingsData> postingsData = index.getPostings().getDocumentsForTerm(termData.getPostingsIndex(), termData.getDocFrequency());
        int fieldId = index.getDocIndex().getFieldDataStore().getField(fieldName).getFieldId();
        PriorityQueue<RankedDocument<Document>> result = new PriorityQueue<RankedDocument<Document>>();
        // Normalize scores and add to result set
        while (postingsData.hasNext()) {
            PostingsData data = postingsData.next();
            long docId = data.getDocumentId();
            Document doc = index.getDocIndex().getDocument(docId);
            int tf = 0;
            for (TermOccurrence occurrence : data.getPositions()) {
                if (occurrence.getFieldId() == fieldId) {
                    tf++;
                }
            }
            if (tf > 0) {
                // Return pure TF score
                result.add(new RankedDocument<Document>(doc, tf));
            }
        }
        return result.iterator();
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public double getBoost() {
        return super.getBoost();
    }

    public Term getTerm() {
        return term;
    }
}
