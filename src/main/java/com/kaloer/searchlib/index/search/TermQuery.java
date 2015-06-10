package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.Document;
import com.kaloer.searchlib.index.InvertedIndex;
import com.kaloer.searchlib.index.postings.PostingsData;
import com.kaloer.searchlib.index.TermDictionary;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.util.IOIterator;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Created by mkaloer on 05/05/15.
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
        if(termData == null) {
            // No results
            return Collections.emptyIterator();
        }
        final IOIterator<PostingsData> postingsData = index.getPostings().getDocumentsForTerm(termData.getPostingsIndex(), termData.getDocFrequency());
        PriorityQueue<RankedDocument<Document>> result = new PriorityQueue<RankedDocument<Document>>();
        // Normalize scores and add to result set
        while (postingsData.hasNext()) {
            PostingsData data = postingsData.next();
            long docId = data.getDocumentId();
            Document doc = index.getDocIndex().getDocument(docId);
            // Pure TF score
            result.add(new RankedDocument<Document>(doc, data.getPositions().size()));
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
