package com.kaloer.searchlib.index;

import org.apache.directory.mavibot.btree.exception.BTreeAlreadyManagedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mkaloer on 12/04/15.
 */
public class InvertedIndex {

    private TermDictionary dictionary;
    private DocumentIndex docIndex;
    private Postings postings;

    public InvertedIndex(IndexConfig conf) {
        this.docIndex = conf.getDocumentIndex();
        this.dictionary = conf.getTermDictionary();
        this.postings = conf.getPostings();
    }

    public List<Document> findDocuments(String term) throws IOException {
        TermDictionary.TermData termData = dictionary.findTerm(term);
        if(termData == null) {
            // Return empty list
            return new ArrayList<Document>();
        }
        PostingsData[] pResults = postings.getDocumentsForTerm(termData.getPostingsIndex(), termData.getDocFrequency());
        List<Document> docs = new ArrayList<Document>(pResults.length);
        for (PostingsData d : pResults) {
            docs.add(docIndex.getDocument(d.getDocumentId()));
        }
        return docs;
    }

    public void indexDocuments(DocumentStream docStream) {
        for(FieldStream fieldStream : docStream) {
            for(TokenStream tokenStream : fieldStream) {
                for(Token t : tokenStream) {
                    // Index document
                    System.out.println(t.getValue());
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            SequentialPostings p1 = new SequentialPostings("test.db");
            BTreeTermDictionary dic = new BTreeTermDictionary("test.db", 25);
            SequentialDocumentIndex docIndex = new SequentialDocumentIndex("test_doc.db", "test_doc_fields.db");
            IndexConfig conf = new IndexConfig().setTermDictionary(dic).setDocumentIndex(docIndex).setPostings(p1);
            InvertedIndex index = new InvertedIndex(conf);

            System.out.println(dic.findTerm("Test1"));
            System.out.println(dic.findTerm("Test2"));
            System.out.println(dic.findTerm("Test3"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BTreeAlreadyManagedException e) {
            e.printStackTrace();
        }
    }

}
