package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.terms.TermType;

import java.io.IOException;

/**
 * Created by mkaloer on 12/04/15.
 */
public abstract class TermDictionary {

    private TermType termType;

    public TermType getTermType() {
        return termType;
    }

    public void setTermType(TermType termType) {
        this.termType = termType;
    }

    public abstract TermData findTerm(Term term) throws IOException;

    public abstract void addTerm(Term term, TermData data) throws IOException;

    public abstract long getTotalDocCount();

    /**
     * Contains information about a term stored in the dictionary.
     */
    public static class TermData {

        private int docFrequency;
        private long termFrequency;
        private long postingsIndex;

        public TermData(int docFrequency, long termFrequency, long postingsIndex) {
            this.docFrequency = docFrequency;
            this.termFrequency = termFrequency;
            this.postingsIndex = postingsIndex;
        }

        public void setDocFrequency(int frequency) {
            this.docFrequency = frequency;
        }

        public int getDocFrequency() {
            return docFrequency;
        }

        public void setTermFrequency(long termFrequency) {
            this.termFrequency = termFrequency;
        }

        public long getTermFrequency() {
            return termFrequency;
        }

        public void setPostingsIndex(long postingsIndex) {
            this.postingsIndex = postingsIndex;
        }

        public long getPostingsIndex() {
            return postingsIndex;
        }

    }
}
