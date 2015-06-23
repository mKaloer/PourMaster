package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.postings.Postings;

/**
 * Configuration class for the inverted index. Used to configure the properties of the index.
 */
public class IndexConfig {

    private DocumentIndex docIndex;
    private TermDictionary termDictionary;
    private Postings postings;
    private String documentTypeFilePath;

    public IndexConfig setDocumentIndex(DocumentIndex docIndex) {
        this.docIndex = docIndex;
        return this;
    }

    public DocumentIndex getDocumentIndex() {
        return docIndex;
    }

    public IndexConfig setTermDictionary(TermDictionary termDictionary) {
        this.termDictionary = termDictionary;
        return this;
    }

    public TermDictionary getTermDictionary() {
        return termDictionary;
    }

    public IndexConfig setPostings(Postings postings) {
        this.postings = postings;
        return this;
    }

    public Postings getPostings() {
        return postings;
    }

    public String getDocumentTypeFilePath() {
        return documentTypeFilePath;
    }

    public IndexConfig setDocumentTypeFilePath(String documentTypeFilePath) {
        this.documentTypeFilePath = documentTypeFilePath;
        return this;
    }
}
