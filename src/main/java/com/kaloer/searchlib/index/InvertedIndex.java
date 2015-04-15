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


    }

    public static void main(String[] args) {
        try {
            SequentialPostings p1 = new SequentialPostings("test.db");
            System.out.println(p1.insertTerm(new PostingsData[] {new PostingsData(1, new long[]{1, 5, 8})}));
            //System.out.println(p1.insertTerm(new Postings.PostingsData(2, new long[] {1, 5, 8})));
            System.out.println(p1.getDocumentsForTerm(7680, 1)[0].getDocumentId());
            System.out.println(p1.getDocumentsForTerm(7712, 1)[0].getDocumentId());
            if(1==1)return;
            BTreeTermDictionary dic = new BTreeTermDictionary("test.db", 25);


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
