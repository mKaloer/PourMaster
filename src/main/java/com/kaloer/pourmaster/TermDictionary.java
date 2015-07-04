package com.kaloer.pourmaster;

import com.kaloer.pourmaster.terms.Term;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract class for the term dictionary used for mapping terms to corresponding term data.
 */
public abstract class TermDictionary {

    public abstract TermData findTerm(Term term) throws IOException;

    public abstract List<TermData> findTerm(Term prefix, Term suffix) throws IOException;

    public abstract void addTerm(Term term, TermData data) throws IOException;

    public abstract long getTotalDocCount();

    abstract void deleteAll() throws IOException;

    /**
     * Initializes the TermDictionary.
     * @param conf The configuration used for creating the index.
     * @throws IOException Thrown if term file cannot be created or read correctly.
     */
    public abstract void init(IndexConfig conf) throws IOException;

    /**
     * Contains information about a term stored in the dictionary.
     */
    public static class TermData {

        private HashMap<Integer, Integer> fieldDocFrequency; // fieldId -> docFrequency
        private int docFrequency;
        private long postingsIndex;

        public TermData(int docFrequency, HashMap<Integer, Integer> fieldDocFrequency, long postingsIndex) {
            this.docFrequency = docFrequency;
            this.fieldDocFrequency = fieldDocFrequency;
            this.postingsIndex = postingsIndex;
        }

        public void setFieldDocFrequency(int fieldId, int frequency) {
            this.fieldDocFrequency.put(fieldId, frequency);
        }

        public void setFieldDocFrequency(HashMap<Integer, Integer> frequency) {
            this.fieldDocFrequency = frequency;
        }

        public int getFieldDocFrequency(int fieldId) {
            return fieldDocFrequency.get(fieldId);
        }

        public HashMap<Integer, Integer> getFieldDocFrequency() {
            return fieldDocFrequency;
        }

        public int getDocFrequency() {
            return docFrequency;
        }

        public void setDocFrequency(int docFrequency) {
            this.docFrequency = docFrequency;
        }

        public void setPostingsIndex(long postingsIndex) {
            this.postingsIndex = postingsIndex;
        }

        public long getPostingsIndex() {
            return postingsIndex;
        }

    }
}
