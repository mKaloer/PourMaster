package com.kaloer.pourmaster;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.kaloer.pourmaster.exceptions.ConflictingFieldTypesException;
import com.kaloer.pourmaster.fields.Field;
import com.kaloer.pourmaster.fields.FieldData;
import com.kaloer.pourmaster.postings.Postings;
import com.kaloer.pourmaster.search.Query;
import com.kaloer.pourmaster.search.RankedDocument;
import com.kaloer.pourmaster.search.RankedDocumentId;
import com.kaloer.pourmaster.terms.Term;
import com.kaloer.pourmaster.terms.TermOccurrence;
import com.kaloer.pourmaster.util.Tuple;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Logger;

/**
 * The main class for the inverted index used for lookup.
 */
public class InvertedIndex {

    // TODO: Fuzzy queries
    // TODO: Scoring class
    // TODO: Index compression
    // TODO: Query parser (see https://github.com/jparsec/jparsec )

    private final static Logger LOGGER = Logger.getLogger(InvertedIndex.class.getName());

    // Number of documents per index iteration
    private final static int DOCS_PER_ITERATION = 10000;

    private DocumentTypeStore docTypeStore;
    private TermDictionary dictionary;
    private DocumentIndex docIndex;
    private FieldNormsStore fieldNormsStore;
    private Postings postings;
    private IndexConfig config;

    public InvertedIndex(IndexConfig conf) throws IOException, ReflectiveOperationException {
        this.config = conf;
        new File(conf.getBaseDirectory()).mkdirs();
        this.docIndex = conf.getDocumentIndex().newInstance();
        this.docIndex.init(conf);
        this.dictionary = conf.getTermDictionary().newInstance();
        this.dictionary.init(conf);
        this.docTypeStore = new DocumentTypeStore(conf.getDocumentTypeFilePath());
        this.postings = conf.getPostings().newInstance();
        this.postings.init(conf);
        this.fieldNormsStore = new FieldNormsStore(conf.getFieldNormsFilePath(),
                this.docIndex.getFieldDataStore().getFieldCount(),
                this.docIndex.getDocumentCount());
    }

    public List<RankedDocument> search(Query query) throws IOException, ReflectiveOperationException {
        return search(query, -1);
    }

    public List<RankedDocument> search(Query query, int count) throws IOException, ReflectiveOperationException {
        Iterator<RankedDocumentId> result = query.search(this);
        // Convert documents to original document format
        ArrayList<RankedDocument> docs = new ArrayList<RankedDocument>();
        while (result.hasNext() && (count == -1 || docs.size() < count)) {
            RankedDocumentId dId = result.next();
            Document d = getDocIndex().getDocument(dId.getDocument());
            // Create result object
            Class docType = this.docTypeStore.getDocumentType(d.getDocumentType());
            Object doc = docType.newInstance();
            // Set object fields
            for (FieldData fieldData : d.getFields()) {
                if (fieldData.getField().isStored()) {
                    docType.getField(fieldData.getField().getFieldName()).set(doc, fieldData.getValue());
                }
            }
            docs.add(new RankedDocument(doc, dId.getScore()));
        }
        return docs;
    }

    public void indexDocuments(Iterable<Object> docStream) throws IOException, ReflectiveOperationException {
        this.indexDocuments(docStream.iterator());
    }

    public void indexDocuments(Iterator<Object> docStream) throws IOException, ReflectiveOperationException {
        // Clear index
        deleteAll();

        String tmpDir = config.getTmpDir();
        PartialIndexData partialIndex = new PartialIndexData();
        ArrayList<String> partialFiles = new ArrayList<String>(); // Partial files paths
        // Term indices in (per partial file)
        ArrayList<ArrayList<Tuple<Term, Long>>> termIndices = new ArrayList<ArrayList<Tuple<Term, Long>>>();
        final HashMap<Term, TermDictionary.TermData> tempTermData = new HashMap<Term, TermDictionary.TermData>();
        // Mapping from term to document frequencies (per partial file)
        HashMap<Term, List<Integer>> docFrequencies = new HashMap<Term, List<Integer>>(DOCS_PER_ITERATION);
        ArrayList<FieldNormsStore> fieldNormsStores = new ArrayList<FieldNormsStore>();
        FieldNormsStore normsStore = new FieldNormsStore(tmpDir + "/fns_" + fieldNormsStores.size(), 256, DOCS_PER_ITERATION);
        fieldNormsStores.add(normsStore);

        HashMap<String, com.kaloer.pourmaster.fields.Field> fieldIds = new HashMap<String, com.kaloer.pourmaster.fields.Field>();
        long docId = 0;
        while (docStream.hasNext()) {
            Object document = docStream.next();
            ArrayList<FieldData> fields = new ArrayList<FieldData>();
            HashSet<Term> termsInDoc = new HashSet<Term>();
            for (java.lang.reflect.Field f : document.getClass().getFields()) {
                com.kaloer.pourmaster.annotations.Field field = f.getAnnotation(com.kaloer.pourmaster.annotations.Field.class);
                if (field != null) {
                    // Check if field already exists - if not, create it
                    if (!fieldIds.containsKey(f.getName())) {
                        com.kaloer.pourmaster.fields.Field fieldInfo = new com.kaloer.pourmaster.fields.Field();
                        fieldInfo.setFieldName(f.getName());
                        fieldInfo.setFieldId(fieldIds.size());
                        fieldInfo.setFieldType(field.type().newInstance());
                        fieldInfo.setIsIndexed(field.indexed());
                        fieldInfo.setIsStored(field.stored());
                        fieldIds.put(f.getName(), fieldInfo);
                    } else {
                        com.kaloer.pourmaster.fields.Field fieldInfo = fieldIds.get(f.getName());
                        if (!field.type().equals(fieldInfo.getFieldType().getClass())) {
                            throw new ConflictingFieldTypesException(
                                    String.format("Conflicting field types for %s: %s and %s",
                                            fieldInfo.getFieldName(), field.type(), fieldInfo.getFieldType().getClass()));
                        }
                    }
                    com.kaloer.pourmaster.fields.Field fieldInfo = fieldIds.get(f.getName());
                    if (field.indexed()) {
                        int numTokens = 0;
                        Iterator<Token> tokens = field.indexAnalyzer().newInstance().analyze(f.get(document));
                        while (tokens.hasNext()) {
                            numTokens++;
                            // Index document
                            Token t = tokens.next();
                            partialIndex.addPositionForTerm(t, docId, fieldInfo.getFieldId());

                            // Update document frequency mapping if not already added (only add once per unique term)
                            if (!termsInDoc.contains(t.getValue())) {
                                termsInDoc.add(t.getValue());

                                // Update document frequency
                                if (!docFrequencies.containsKey(t.getValue())) {
                                    docFrequencies.put(t.getValue(), new ArrayList<Integer>(
                                            Collections.nCopies(partialFiles.size() + 1, Integer.valueOf(0))));
                                } else if (docFrequencies.get(t.getValue()).size() <= partialFiles.size()) {
                                    List<Integer> termList = docFrequencies.get(t.getValue());
                                    while (termList.size() <= partialFiles.size()) {
                                        termList.add(0);
                                    }
                                }

                                // Add new doc frequency
                                int oldFreq = docFrequencies.get(t.getValue()).get(partialFiles.size());
                                docFrequencies.get(t.getValue()).set(partialFiles.size(), oldFreq + 1);
                            }
                        }

                        // Set field length
                        normsStore.setFieldNorm(fieldInfo.getFieldId(), docId % DOCS_PER_ITERATION, 1.0f / (float) numTokens);
                    }
                    FieldData fieldData = new FieldData();
                    fieldData.setField(fieldInfo);
                    fieldData.setValue(f.get(document));
                    fields.add(fieldData);
                }

            }
            if ((docId + 1) % DOCS_PER_ITERATION == 0) {
                LOGGER.info(String.format("Indexed %d documents", docId + 1));
                LOGGER.info("Writing partial file...");
                String outputFile = new File(tmpDir, String.format("postings_%d.part", partialFiles.size())).getAbsolutePath();
                termIndices.add(writePartialPostings(outputFile, partialIndex, tempTermData));
                partialFiles.add(outputFile);
                partialIndex.clear();
                normsStore = new FieldNormsStore(tmpDir + File.separator + "fns_" + fieldNormsStores.size(), 256, DOCS_PER_ITERATION);
                fieldNormsStores.add(normsStore);
                LOGGER.info("Partial write successful.");
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
        if (partialIndex.dictionary.size() > 0) {
            String outputFile = new File(tmpDir, String.format("postings_%d.part", partialFiles.size())).getAbsolutePath();
            termIndices.add(writePartialPostings(outputFile, partialIndex, tempTermData));
            partialFiles.add(outputFile);
        }

        LOGGER.info("Merging partial files...");
        // Merge partial files
        final HashMap<Term, Long> indices = postings.mergePartialPostingsFiles(partialFiles, termIndices, docFrequencies);

        // Insert into dictionary
        this.dictionary.bulkInsertData(
                ImmutableList.copyOf(Collections2.transform(tempTermData.entrySet(),
                        new Function<Map.Entry<Term, TermDictionary.TermData>, Tuple<Term, TermDictionary.TermData>>() {
            public Tuple<Term, TermDictionary.TermData> apply(Map.Entry<Term, TermDictionary.TermData> termEntry) {
                termEntry.getValue().setPostingsIndex(indices.get(termEntry.getKey()));
                return new Tuple<Term, TermDictionary.TermData>(termEntry.getKey(), termEntry.getValue());
            }
        })));

        // Merge norms stores
        fieldNormsStore = new FieldNormsStore(config.getFieldNormsFilePath(), fieldIds.size(), docId);
        for (long i = 0; i < docId; i++) {
            FieldNormsStore partial = fieldNormsStores.get((int) (i / DOCS_PER_ITERATION));
            for (Field f : fieldIds.values()) {
                fieldNormsStore.setFieldNorm(f.getFieldId(), i, partial.getFieldNorm(f.getFieldId(), i % DOCS_PER_ITERATION));
            }
        }

        // Delete tmp files
        for (String tmpFileName : partialFiles) {
            File f = new File(tmpFileName);
            if (f.exists()) {
                f.delete();
            }
        }
        LOGGER.info(String.format("Indexed %d documents", docId + 1));
        LOGGER.info("Indexing completed.");
    }

    private ArrayList<Tuple<Term, Long>> writePartialPostings(String outputFile, PartialIndexData partialIndex, HashMap<Term,
            TermDictionary.TermData> tempTermData) throws IOException {
        ArrayList<Tuple<Term, Long>> termIndices = this.postings.writePartialPostingsToFile(outputFile, partialIndex);
        for (Tuple<Term, Long> term : termIndices) {
            // Aggregate document frequency and term frequency
            int docFreq = 0;
            HashMap<Integer, Integer> fieldDocFreq = new HashMap<Integer, Integer>();
            for (Long docId : partialIndex.getDocsForTerm(term.getFirst())) {
                docFreq += 1;
                // Update per-field doc frequency
                for (TermOccurrence occurrence : partialIndex.getPositionsForDoc(term.getFirst(), docId)) {
                    if (!fieldDocFreq.containsKey(occurrence.getFieldId())) {
                        fieldDocFreq.put(occurrence.getFieldId(), 1);
                    } else {
                        int newFreq = fieldDocFreq.get(occurrence.getFieldId()) + 1;
                        fieldDocFreq.put(occurrence.getFieldId(), newFreq);
                    }
                }
            }
            // Update dictionary
            if (!tempTermData.containsKey(term.getFirst())) {
                tempTermData.put(term.getFirst(), new TermDictionary.TermData(docFreq, fieldDocFreq, term.getSecond()));
            } else {
                // Term already exists: Increase existing docFreq and fieldDocFreq
                TermDictionary.TermData existingData = tempTermData.get(term.getFirst());
                existingData.setDocFrequency(existingData.getDocFrequency() + docFreq);
                for (Map.Entry<Integer, Integer> entry : fieldDocFreq.entrySet()) {
                    if (!existingData.getFieldDocFrequency().containsKey(entry.getKey())) {
                        existingData.getFieldDocFrequency().put(entry.getKey(), entry.getValue());
                    } else {
                        int newVal = existingData.getFieldDocFrequency().get(entry.getKey()) + entry.getValue();
                        existingData.getFieldDocFrequency().put(entry.getKey(), newVal);
                    }
                }
            }
        }

        return termIndices;
    }

    public void deleteAll() throws IOException {
        docTypeStore.deleteAll();
        dictionary.deleteAll();
        docIndex.deleteAll();
        postings.deleteAll();
        fieldNormsStore.deleteAll();
    }

    public void close() {
        dictionary.close();
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

    public IndexConfig getConfig() {
        return config;
    }

    public FieldNormsStore getFieldNormsStore() {
        return fieldNormsStore;
    }

}
