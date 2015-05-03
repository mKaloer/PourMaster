package com.kaloer.searchlib.index;

import com.kaloer.searchlib.index.fields.FieldTypeStore;
import com.kaloer.searchlib.index.terms.StringTermType;

import javax.print.Doc;

/**
 * Created by mkaloer on 13/04/15.
 */
public class IndexConfig {

    private DocumentIndex docIndex;
    private TermDictionary termDictionary;
    private Postings postings;
    private String fieldTypeStorePath;

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
}
