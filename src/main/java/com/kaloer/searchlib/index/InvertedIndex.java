package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.annotations.Field;
import com.kaloer.searchlib.index.exceptions.ConflictingFieldTypesException;
import com.kaloer.searchlib.index.fields.FieldData;
import com.kaloer.searchlib.index.search.Query;
import com.kaloer.searchlib.index.search.RankedDocument;
import com.kaloer.searchlib.index.terms.Term;
import com.kaloer.searchlib.index.terms.TermOccurrence;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by mkaloer on 12/04/15.
 */
public class InvertedIndex {

    private DocumentTypeStore docTypeStore;
    private TermDictionary dictionary;
    private DocumentIndex docIndex;
    private Postings postings;
    private IndexConfig config;

    public InvertedIndex(IndexConfig conf) throws IOException, ReflectiveOperationException {
        this.config = conf;
        this.docIndex = conf.getDocumentIndex();
        this.dictionary = conf.getTermDictionary();
        this.docTypeStore = new DocumentTypeStore(conf.getDocumentTypeFilePath());
        this.postings = conf.getPostings();
    }

    public List<RankedDocument> search(Query query, int count) throws IOException, ReflectiveOperationException {
        Iterator<RankedDocument<Document>> result = query.search(this);
        // Convert documents to original document format
        ArrayList<RankedDocument> docs = new ArrayList<RankedDocument>();
        while (result.hasNext() && (count == -1 || docs.size() < count)) {
            RankedDocument<Document> d = result.next();
            // Create result object
            Class docType = this.docTypeStore.getDocumentType(d.getDocument().getDocumentType());
            Object doc = docType.newInstance();
            // Set object fields
            for(FieldData fieldData : d.getDocument().getFields()) {
                if(fieldData.getField().isStored()) {
                    docType.getField(fieldData.getField().getFieldName()).set(doc, fieldData.getValue());
                }
            }
            docs.add(new RankedDocument(doc, d.getScore()));
        }
        return docs;
    }

    public void indexDocuments(Iterable<Object> docStream, File tmpDir) throws IOException, ReflectiveOperationException {
        this.indexDocuments(docStream.iterator(), tmpDir);
    }

    public void indexDocuments(Iterator<Object> docStream, File tmpDir) throws IOException, ReflectiveOperationException {
        // Dictionary mapping terms to <docId, postingsData>
        HashMap<Term, HashMap<Long, PostingsData>> dictionary = new HashMap<Term, HashMap<Long, PostingsData>>();
        ArrayList<String> partialFiles = new ArrayList<String>();
        ArrayList<ArrayList<Map.Entry<Term, Long>>> termIndices = new ArrayList<ArrayList<Map.Entry<Term, Long>>>();
        // Mapping from term to document frequencies (per partial file)
        ArrayList<HashMap<Term, Integer>> docFrequencies = new ArrayList<HashMap<Term, Integer>>();
        docFrequencies.add(new HashMap<Term, Integer>());

        HashMap<String, com.kaloer.searchlib.index.fields.Field> fieldIds = new HashMap<String, com.kaloer.searchlib.index.fields.Field>();
        long docId = 0;
        while (docStream.hasNext()) {
            Object document = docStream.next();
            ArrayList<FieldData> fields = new ArrayList<FieldData>();
            for(java.lang.reflect.Field f : document.getClass().getFields()) {
                Field field = f.getAnnotation(Field.class);
                if(field != null) {
                    // Check if field already exists - if not, create it
                    if(!fieldIds.containsKey(f.getName())) {
                        com.kaloer.searchlib.index.fields.Field fieldInfo = new com.kaloer.searchlib.index.fields.Field();
                        fieldInfo.setFieldName(f.getName());
                        fieldInfo.setFieldId(fieldIds.size());
                        fieldInfo.setFieldType(field.type().newInstance());
                        fieldInfo.setIsIndexed(field.indexed());
                        fieldInfo.setIsStored(field.stored());
                        fieldIds.put(f.getName(), fieldInfo);
                    } else {
                        com.kaloer.searchlib.index.fields.Field fieldInfo = fieldIds.get(f.getName());
                        if(!field.type().equals(fieldInfo.getFieldType().getClass())) {
                            throw new ConflictingFieldTypesException(
                                    String.format("Conflicting field types for %s: %s and %s",
                                            fieldInfo.getFieldName(), field.type(), fieldInfo.getFieldType().getClass()));
                        }
                    }
                    com.kaloer.searchlib.index.fields.Field fieldInfo = fieldIds.get(f.getName());
                    if(field.indexed()) {
                        Iterator<Token> tokens = field.indexAnalyzer().newInstance().analyze(f.get(document));
                        while (tokens.hasNext()) {
                            // Index document
                            HashMap<Long, PostingsData> postings;
                            Token t = tokens.next();
                            if (!dictionary.containsKey(t.getValue())) {
                                postings = new HashMap<Long, PostingsData>();
                                dictionary.put(t.getValue(), postings);
                            } else {
                                postings = dictionary.get(t.getValue());
                            }

                            if (!postings.containsKey(docId)) {
                                postings.put(docId, new PostingsData(docId, t.getPosition(), fieldInfo.getFieldId()));
                            } else {
                                postings.get(docId).addPosition(t.getPosition(), fieldInfo.getFieldId());
                            }

                            // Update document frequency mapping
                            if (!docFrequencies.get(partialFiles.size()).containsKey(t.getValue())) {
                                // New term in this file. Add doc freq 0
                                docFrequencies.get(partialFiles.size()).put(t.getValue(), 0);
                            }
                            // Add new doc frequency
                            int oldFreq = docFrequencies.get(partialFiles.size()).get(t.getValue());
                            docFrequencies.get(partialFiles.size()).put(t.getValue(), oldFreq + 1);
                        }
                    }

                    FieldData fieldData = new FieldData();
                    fieldData.setField(fieldInfo);
                    fieldData.setValue(f.get(document));
                    fields.add(fieldData);
                }

            }
            if((docId + 1) % 1000 == 0) {
                String outputFile = new File(tmpDir, String.format("postings_%d.part", partialFiles.size())).getAbsolutePath();
                termIndices.add(writePartialPostings(outputFile, dictionary));
                partialFiles.add(outputFile);
                dictionary.clear();
                // Add new docFrequencies map for next partial file
                docFrequencies.add(new HashMap<Term, Integer>());
            }

            // Add document to document index and field store
            Document doc = new Document();
            doc.setDocumentId(docId);
            doc.setFields(fields);
            doc.setDocumentType(this.docTypeStore.getOrCreateDocumentType(document.getClass()));
            docIndex.addDocument(doc);
            docId++;
        }
        // Write remaining postings
        String outputFile = new File(tmpDir, String.format("postings_%d.part", partialFiles.size())).getAbsolutePath();
        termIndices.add(writePartialPostings(outputFile, dictionary));
        partialFiles.add(outputFile);

        // Merge partial files
        HashMap<Term, Long> indices = postings.mergePartialPostingsFiles(partialFiles, termIndices, docFrequencies);
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
            HashMap<Integer, Integer> fieldDocFreq = new HashMap<Integer, Integer>();
            for(Long docId : dictionary.get(term.getKey()).keySet()) {
                docFreq += 1;
                // Update per-field doc frequency
                for (TermOccurrence occurrence : dictionary.get(term.getKey()).get(docId).getPositions()) {
                    if(!fieldDocFreq.containsKey(occurrence.getFieldId())) {
                        fieldDocFreq.put(occurrence.getFieldId(), 1);
                    } else {
                        int newFreq = fieldDocFreq.get(occurrence.getFieldId()) + 1;
                        fieldDocFreq.put(occurrence.getFieldId(), newFreq);
                    }
                }
            }
            // Update dictionary
            if(this.dictionary.findTerm(term.getKey()) == null) {
                // First occurrence of this term
                this.dictionary.addTerm(term.getKey(), new TermDictionary.TermData(docFreq, fieldDocFreq, term.getValue()));
            } else {
                // Term already exists: Increase existing docFreq and fieldDocFreq
                TermDictionary.TermData existingData = this.dictionary.findTerm(term.getKey());
                existingData.setDocFrequency(existingData.getDocFrequency() + docFreq);
                for(Map.Entry<Integer, Integer> entry : fieldDocFreq.entrySet()) {
                    if(!existingData.getFieldDocFrequency().containsKey(entry.getKey())) {
                        existingData.getFieldDocFrequency().put(entry.getKey(), entry.getValue());
                    } else {
                        int newVal = existingData.getFieldDocFrequency().get(entry.getKey()) + entry.getValue();
                        existingData.getFieldDocFrequency().put(entry.getKey(), newVal);
                    }
                }
            }
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
