package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.postings.PostingsData;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.terms.TermOccurrence;
import com.kaloer.searchlib.index.util.Tuple;

import java.util.*;

/**
 * Contains a partial mapping {@link Term} to documentID to {@link PostingsData}.
 */
public class PartialIndexData {

    // Dictionary mapping terms to <docId, postingsData>
    HashMap<Term, HashMap<Long, PostingsData>> dictionary;

    public PartialIndexData() {
        dictionary = new HashMap<Term, HashMap<Long, PostingsData>>();
    }

    /**
     * Gets the postings data for the given term.
     * @param t The term
     * @return A mapping docId -> postingsData
     */
    private HashMap<Long, PostingsData> getPostingsForTerm(Term t) {
        HashMap<Long, PostingsData> postings;
        if (!dictionary.containsKey(t)) {
            postings = new HashMap<Long, PostingsData>();
            dictionary.put(t, postings);
        } else {
            postings = dictionary.get(t);
        }
        return postings;
    }

    /**
     * Adds a position for a given term in a document.
     * @param t The term to add an occurrence of.
     * @param docId The document id containing the term.
     * @param fieldId The field in the document containing the term.
     */
    public void addPositionForTerm(Token t, long docId, int fieldId) {
        HashMap<Long, PostingsData> postings = getPostingsForTerm(t.getValue());
        if (!postings.containsKey(docId)) {
            postings.put(docId, new PostingsData(docId, t.getPosition(), fieldId));
        } else {
            postings.get(docId).addPosition(t.getPosition(), fieldId);
        }
    }

    /**
     * Gets the documents containing the given term.
     * @param term The term.
     * @return Document ids of all documents containing the term.
     */
    public Set<Long> getDocsForTerm(Term term) {
        return dictionary.get(term).keySet();
    }

    /**
     * Gets the positions of specific term in a given document.
     * @param term The term.
     * @param docId The document id.
     * @return A list of all occurrences of the term in the document.
     */
    public ArrayList<TermOccurrence> getPositionsForDoc(Term term, Long docId) {
        return dictionary.get(term).get(docId).getPositions();
    }

    /**
     * Gets the postings data sorted by term.
     * @return
     */
    public Iterable<Tuple<Term, HashMap<Long, PostingsData>>> getSortedPostings() {
        final TreeMap<Term, HashMap<Long, PostingsData>> sortedDict = new TreeMap<Term, HashMap<Long, PostingsData>>();
        sortedDict.putAll(dictionary);
        return new PostingsDataIterator(sortedDict.entrySet().iterator());
    }

    /**
     * Clears the dictionary.
     */
    public void clear() {
        dictionary.clear();
    }

    /**
     * Iterator wrapper so it contains Tuples rather than Map.Entry.
     */
    private static class PostingsDataIterator implements Iterable<Tuple<Term, HashMap<Long, PostingsData>>>, Iterator<Tuple<Term, HashMap<Long, PostingsData>>> {

        private Iterator<Map.Entry<Term, HashMap<Long, PostingsData>>> iterator;

        public PostingsDataIterator(Iterator<Map.Entry<Term, HashMap<Long, PostingsData>>> source) {
            this.iterator = source;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Tuple<Term, HashMap<Long, PostingsData>> next() {
            Map.Entry<Term, HashMap<Long, PostingsData>> item = iterator.next();
            return new Tuple<Term, HashMap<Long, PostingsData>>(item.getKey(), item.getValue());
        }

        public void remove() {
            iterator.remove();
        }

        public Iterator<Tuple<Term, HashMap<Long, PostingsData>>> iterator() {
            return this;
        }
    }
}
