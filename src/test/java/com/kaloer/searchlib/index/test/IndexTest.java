package com.kaloer.searchlib.index.test;

import com.kaloer.searchlib.index.*;
import org.apache.directory.mavibot.btree.exception.BTreeAlreadyManagedException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by mkaloer on 13/04/15.
 */
public class IndexTest {

    InvertedIndex index;

    @After
    public void tearDown() {
        if(index != null) {
            // TODO: Delete index
        }
    }


    @Test
    public void testTest() throws BTreeAlreadyManagedException {
        IndexConfig conf = null;
        try {
            conf = new IndexConfig().
                    setDocumentIndex(new SequentialDocumentIndex("docs.idx", "docs_fields.idx"))
                    .setPostings(new SequentialPostings("posings.db"))
                    .setTermDictionary(new BTreeTermDictionary("dict.db"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        InvertedIndex index = new InvertedIndex(conf);
        try {
            List<Document> docs = index.findDocuments("Hello");
            Assert.assertEquals("Expected empty result set", 0, docs.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
