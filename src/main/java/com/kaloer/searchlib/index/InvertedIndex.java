package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.FieldTypeStore;
import com.kaloer.searchlib.index.search.DocumentScore;
import com.kaloer.searchlib.index.search.Query;
import com.kaloer.searchlib.index.search.RankedDocument;
import com.kaloer.searchlib.index.terms.Term;
import com.sun.xml.internal.xsom.impl.scd.Iterators;
import org.apache.commons.collections.IteratorUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by mkaloer on 12/04/15.
 */
public class InvertedIndex {

    private TermDictionary dictionary;
    private DocumentIndex docIndex;
    private Postings postings;
    private IndexConfig config;

    public InvertedIndex(IndexConfig conf) throws IOException, ReflectiveOperationException {
        this.config = conf;
        this.docIndex = conf.getDocumentIndex();
        this.dictionary = conf.getTermDictionary();
        this.postings = conf.getPostings();
    }

    public List<RankedDocument> search(Query query, int count) throws IOException {
        Iterator<RankedDocument> result = query.search(this);
        ArrayList<RankedDocument> docs = new ArrayList<RankedDocument>();
        while (result.hasNext() && (count == -1 || docs.size() < count)) {
            docs.add(result.next());
        }
        return docs;
    }

    public void indexDocuments(DocumentStream docStream, File tmpDir) throws IOException, ReflectiveOperationException {
        // Dictionary mapping terms to <docId, postingsData>
        HashMap<Term, HashMap<Long, PostingsData>> dictionary = new HashMap<Term, HashMap<Long, PostingsData>>();
        ArrayList<String> partialFiles = new ArrayList<String>();
        ArrayList<ArrayList<Map.Entry<Term, Long>>> termIndices = new ArrayList<ArrayList<Map.Entry<Term, Long>>>();
        long docId = 0;
        for(FieldStream fieldStream : docStream) {
            for(TokenStream<?, ?> tokenStream : fieldStream) {
                for(Token<? extends Term> t : tokenStream) {
                    // Index document
                    HashMap<Long, PostingsData> postings;
                    if(!dictionary.containsKey(t.getValue())) {
                        postings = new HashMap<Long, PostingsData>();
                        dictionary.put(t.getValue(), postings);
                    } else {
                        postings = dictionary.get(t.getValue());
                    }
                    if(!postings.containsKey(docId)) {
                        postings.put(docId, new PostingsData(docId, t.getPosition(), tokenStream.getField().getFieldId()));
                    } else {
                        postings.get(docId).addPosition(t.getPosition(), tokenStream.getField().getFieldId());
                    }
                }
            }

            if((docId + 1) % 1000 == 0) {
                String outputFile = new File(tmpDir, String.format("postings_%d.part", partialFiles.size())).getAbsolutePath();
                termIndices.add(writePartialPostings(outputFile, dictionary));
                partialFiles.add(outputFile);
                dictionary.clear();
            }

            // Add document to document index and field store
            docIndex.addDocument(fieldStream.getDocument());
            docId++;
        }
        // Write remaining postings
        String outputFile = new File(tmpDir, String.format("postings_%d.part", partialFiles.size())).getAbsolutePath();
        termIndices.add(writePartialPostings(outputFile, dictionary));
        partialFiles.add(outputFile);

        // Merge partial files
        HashMap<Term, Long> indices = postings.mergePartialPostingsFiles(partialFiles, termIndices);
        // Update dictionary with new pointers
        for(Map.Entry<Term, Long> t : indices.entrySet()) {
            this.dictionary.findTerm(t.getKey()).setPostingsIndex(t.getValue());
        }

    }

    private ArrayList<Map.Entry<Term, Long>> writePartialPostings(String outputFile, HashMap<Term, HashMap<Long, PostingsData>> dictionary) throws IOException {
        ArrayList<Map.Entry<Term, Long>> termIndices = this.postings.writePartialPostingsToFile(outputFile, dictionary);
        Collections.sort(termIndices, new Comparator<Map.Entry<Term, Long>>() {
            public int compare(Map.Entry<Term, Long> o1, Map.Entry<Term, Long> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        for(Map.Entry<Term, Long> term : termIndices) {
            // Aggregate document frequency and term frequency
            int docFreq = 0;
            long termFreq = 0;
            for(Long docId : dictionary.get(term.getKey()).keySet()) {
                docFreq += 1;
                termFreq += dictionary.get(term.getKey()).get(docId).getPositions().size();
            }
            this.dictionary.addTerm(term.getKey(), new TermDictionary.TermData(docFreq, termFreq, term.getValue()));
        }

        dictionary.clear();
        return termIndices;
    }

    public DocumentIndex getDocIndex() {
        return docIndex;
    }

    public Postings getPostings() {
        return postings;
    }

    public TermDictionary getDictionary() {
        return dictionary;
    }
}
