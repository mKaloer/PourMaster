package com.kaloer.searchlib.index;

import com.kaloer.searchlib.Term;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by mkaloer on 12/04/15.
 */
public abstract class TermDictionary {

    public abstract TermData findTerm(String term) throws IOException;

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

        public int getDocFrequency() {
            return docFrequency;
        }

        public long getTermFrequency() {
            return termFrequency;
        }

        public long getPostingsIndex() {
            return postingsIndex;
        }

    }
}
