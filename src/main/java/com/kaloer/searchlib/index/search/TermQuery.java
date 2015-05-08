package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.InvertedIndex;
import com.kaloer.searchlib.index.PostingsData;
import com.kaloer.searchlib.index.TermDictionary;
import com.kaloer.searchlib.index.search.Query;
import com.kaloer.searchlib.index.search.RankedDocument;
import com.kaloer.searchlib.index.terms.Term;

import java.io.IOException;
import java.util.*;

/**
 * Created by mkaloer on 05/05/15.
 */
public class TermQuery extends Query {

    private Term term;
    private int fieldId;

    public TermQuery(Term term, int fieldId) {
        this.term = term;
        this.fieldId = fieldId;
    }

    @Override
    public Iterator<RankedDocument> search(InvertedIndex index) throws IOException {
        TermDictionary.TermData termData = index.getDictionary().findTerm(term);
        if(termData == null) {
            // No results
            return Collections.emptyIterator();
        }
        final double idf = Math.log((double) index.getDocIndex().getDocumentCount() / (double) (1 + termData.getDocFrequency()));
        final Iterator<PostingsData> postingsData = index.getPostings().getDocumentsForTerm(termData.getPostingsIndex(), termData.getDocFrequency());
        // TODO: find docs
        return Collections.emptyIterator();
    }

    public int getFieldId() {
        return fieldId;
    }

    @Override
    public double getBoost() {
        return super.getBoost();
    }

    public Term getTerm() {
        return term;
    }
}
