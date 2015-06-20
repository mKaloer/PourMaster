package com.kaloer.searchlib.index.search;

import com.kaloer.searchlib.index.Document;
import com.kaloer.searchlib.index.InvertedIndex;
import com.kaloer.searchlib.index.TermDictionary;
import com.kaloer.searchlib.index.postings.PostingsData;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.terms.TermOccurrence;
import com.kaloer.searchlib.index.util.IOIterator;
import com.kaloer.searchlib.index.util.Tuple;
import org.apache.commons.collections.map.MultiValueMap;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.*;

public class PhraseQuery extends Query {

    private ArrayList<Term> terms = new ArrayList<Term>();
    private ArrayList<Integer> positions = new ArrayList<Integer>();
    private String fieldName;

    /**
     * Adds a term to the end of the query.
     *
     * @param term The term to add.
     */
    public void add(Term term) {
        terms.add(term);

        int termIndex = 0;
        if (positions.size() > 0) {
            termIndex = positions.size();
        }
        positions.add(termIndex);
    }

    /**
     * Adds a term with the given index of the phrase.
     *
     * @param term     The term to add.
     * @param position The index of the term in the phrase.
     */
    public void add(Term term, int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Term position must be non-negative");
        } else if (positions.size() > 0) {
            int largestPos = positions.get(positions.size() - 1);
            if (largestPos == position) {
                throw new IllegalArgumentException("Term positions must be unique");
            } else if (largestPos > position) {
                throw new IllegalArgumentException("Terms must be added in the order they appear in the phrase");
            }
        }
        terms.add(term);
        positions.add(position);
    }

    public PhraseQuery(String fieldName) {
        this.fieldName = fieldName;
    }

    public PhraseQuery(String fieldName, Term... terms) {
        this.fieldName = fieldName;
        for (Term t : terms) {
            this.terms.add(t);
            positions.add(positions.size());
        }
    }

    @Override
    public Iterator<RankedDocument<Document>> search(InvertedIndex index) throws IOException {
        int fieldId = index.getDocIndex().getFieldDataStore().getField(fieldName).getFieldId();
        Set<Tuple<Long, Integer>> docIds = findPhraseMatches(index, fieldId);

        final PriorityQueue<RankedDocument<Document>> result = new PriorityQueue<RankedDocument<Document>>();
        // Use plain tf ("phrase occurrences") as doc score
        for (Tuple<Long, Integer> match : docIds) {
            Document doc = index.getDocIndex().getDocument(match.getFirst());
            RankedDocument<Document> rankedDoc = new RankedDocument<Document>(doc, match.getSecond());
            result.add(rankedDoc);
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

    /**
     * Finds all documentIds of documents containing the phrase.
     *
     * @param index   The inverted index instance.
     * @param fieldId The id of the field containing the phrase.
     * @return A set of docIds containing the phrase.
     * @throws IOException
     */
    private Set<Tuple<Long, Integer>> findPhraseMatches(InvertedIndex index, int fieldId) throws IOException {
        // Order terms by frequency to shrink working set early
        PriorityQueue<PhraseQueryTerm> termList =
                new PriorityQueue<PhraseQueryTerm>(terms.size(), new TermFrequencyComparator());
        for (int i = 0; i < terms.size(); i++) {
            Term t = terms.get(i);
            TermDictionary.TermData data = index.getDictionary().findTerm(t);
            if (data == null) {
                // If a term does not exist, there is no chance of matching the query, so return empty set.
                return new HashSet<Tuple<Long, Integer>>();
            } else {
                termList.add(new PhraseQueryTerm(positions.get(i), t, data));
            }
        }

        // Mapping: docId -> [position1, position2, ..., positionN]. This is iteratively narrowed.
        MultiValueMap resultSet = new MultiValueMap();
        int prevTermPosition = -1; // Stores position of previous term in query. Used for calculating relative position.
        while (!termList.isEmpty()) {
            // Same as resultSet, but contains the intersection of resultSet and the result of current term.
            MultiValueMap workingSet = new MultiValueMap();
            PhraseQueryTerm queryTerm = termList.poll();
            final IOIterator<PostingsData> docs = index.getPostings().getDocumentsForTerm(
                    queryTerm.termData.getPostingsIndex(),
                    queryTerm.termData.getDocFrequency());

            boolean firstTerm = resultSet.isEmpty();

            while (docs.hasNext()) {
                PostingsData postingsData = docs.next();
                // Add every term in first run.
                if (firstTerm) {
                    for (TermOccurrence position : postingsData.getPositions()) {
                        if (position.getFieldId() == fieldId) {
                            workingSet.put(postingsData.getDocumentId(), position.getPosition());
                        }
                    }
                } else {
                    // Intersect resultSet with current postings

                    // If not in working set, skip this doc as it does not contain all terms
                    if (!resultSet.containsKey(postingsData.getDocumentId())) {
                        continue;
                    }

                    // Check if relative positions correspond to query position
                    for (TermOccurrence position : postingsData.getPositions()) {
                        if (position.getFieldId() == fieldId) {
                            // Get position for previous term in document
                            for (long pos : (Collection<Long>) resultSet.get(postingsData.getDocumentId())) {
                                if (position.getPosition() - pos == queryTerm.position - prevTermPosition) {
                                    // Relative positioning matches!
                                    workingSet.put(postingsData.getDocumentId(), position.getPosition());
                                }
                            }
                        }
                    }
                }
            }
            resultSet = workingSet;
            prevTermPosition = queryTerm.position;
        }

        // Count occurrences of each match per document
        Set<Tuple<Long, Integer>> tfResult = new HashSet<Tuple<Long, Integer>>();
        for (Long docId : (Set<Long>) resultSet.keySet()) {
            tfResult.add(new Tuple<Long, Integer>(docId, resultSet.getCollection(docId).size()));
        }

        return tfResult;
    }

    /**
     * Comparator for ordering terms by frequency.
     */
    private static class TermFrequencyComparator implements Comparator<PhraseQueryTerm> {

        public int compare(PhraseQueryTerm o1, PhraseQueryTerm o2) {
            return o1.termData.getDocFrequency() - o2.termData.getDocFrequency();
        }
    }

    private static class PhraseQueryTerm {
        int position;
        Term term;
        TermDictionary.TermData termData;

        PhraseQueryTerm(int position, Term term, TermDictionary.TermData termData) {
            this.position = position;
            this.term = term;
            this.termData = termData;
        }
    }
}
